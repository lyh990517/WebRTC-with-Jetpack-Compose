package com.example.webrtc.client.controller

import android.graphics.Bitmap
import com.example.webrtc.client.event.WebRtcEvent
import com.example.webrtc.client.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.merge
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WebRtcController @Inject constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val localResourceController: Controller.LocalResource,
    private val dataChannelManager: DataChannelManager
) : Controller.WebRtc {
    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }
    private val controllerEvent = MutableSharedFlow<WebRtcEvent>(extraBufferCapacity = 100)

    private var peerConnection: PeerConnection? = null

    override fun connect(roomID: String, isHost: Boolean) {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServer).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            enableRtpDataChannel = true
        }

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            createPeerConnectionObserver(isHost)
        )

        peerConnection?.let(dataChannelManager::initialize)

        peerConnection?.addTrack(localResourceController.getVideoTrack())
        peerConnection?.addTrack(localResourceController.getAudioTrack())

        if (isHost) {
            controllerEvent.tryEmit(WebRtcEvent.Host.SendOffer)
        }
    }

    override fun sendMessage(message: String) {
        dataChannelManager.sendMessage(message)
    }

    override fun sendImage(bitmap: Bitmap) {
        dataChannelManager.sendImage(bitmap)
    }

    override fun sendFile(bytes: ByteArray) {
        dataChannelManager.sendFile(bytes)
    }

    override fun getMessages(): Flow<Message> =
        dataChannelManager.getMessages()


    override fun createOffer() {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                controllerEvent.tryEmit(WebRtcEvent.Host.SetLocalSdp(observer, sdp))

                controllerEvent.tryEmit(WebRtcEvent.Host.SendSdpToGuest(sdp))
            }
        )

        peerConnection?.createOffer(sdpObserver, constraints)
    }

    override fun createAnswer() {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                controllerEvent.tryEmit(WebRtcEvent.Guest.SetLocalSdp(observer, sdp))

                controllerEvent.tryEmit(WebRtcEvent.Guest.SendSdpToHost(sdp))
            }
        )

        peerConnection?.createAnswer(sdpObserver, constraints)
    }

    override fun closeConnection() {
        peerConnection?.close()
    }

    override fun setLocalDescription(sdp: SessionDescription, observer: SdpObserver) {
        peerConnection?.setLocalDescription(observer, sdp)
    }

    override fun setRemoteDescription(sdp: SessionDescription) {
        val sdpObserver = createSdpObserver()

        peerConnection?.setRemoteDescription(sdpObserver, sdp)
    }

    override fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    override fun getEvent(): Flow<WebRtcEvent> = merge(
        controllerEvent.asSharedFlow(),
        dataChannelManager.getEvent()
    )

    private fun createSdpObserver(onSdpCreationSuccess: ((SessionDescription, SdpObserver) -> Unit)? = null) =
        object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                onSdpCreationSuccess?.let { function -> p0?.let { sdp -> function(sdp, this) } }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }

    private fun createPeerConnectionObserver(
        isHost: Boolean
    ): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                controllerEvent.tryEmit(WebRtcEvent.StateChange.Connection(newState))
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                controllerEvent.tryEmit(WebRtcEvent.StateChange.Signaling(p0))
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                controllerEvent.tryEmit(WebRtcEvent.StateChange.IceConnection(p0))
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                controllerEvent.tryEmit(WebRtcEvent.StateChange.IceGathering(p0))
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let { ice ->
                    if (isHost) {
                        controllerEvent.tryEmit(WebRtcEvent.Host.SendIceToGuest(ice))
                    } else {
                        controllerEvent.tryEmit(WebRtcEvent.Guest.SendIceToHost(ice))
                    }
                }
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {}

            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
            override fun onTrack(transceiver: RtpTransceiver?) {
                val track = transceiver?.receiver?.track()
                if (track is VideoTrack) {
                    track.addSink(localResourceController.getRemoteSurface())
                }
            }
        }

    companion object {
        private const val ICE_SERVER_URL = "stun:stun4.l.google.com:19302"
    }
}
