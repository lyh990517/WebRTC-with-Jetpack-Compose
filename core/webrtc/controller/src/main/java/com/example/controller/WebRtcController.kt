package com.example.controller

import com.example.common.WebRtcEvent
import com.example.webrtc.client.Controller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
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
    private val webRtcScope: CoroutineScope,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val localResourceController: Controller.LocalResource,
    private val dataChannelManager: DataChannelManager
) : Controller.WebRtc {
    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }
    private val controllerEvent = MutableSharedFlow<WebRtcEvent>()

    private var peerConnection: PeerConnection? = null

    override fun connect(roomID: String, isHost: Boolean) {
        webRtcScope.launch {
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
                controllerEvent.emit(WebRtcEvent.Host.SendOffer)
            }
        }
    }

    override fun sendMessage(data: Any) {
        dataChannelManager.sendMessage(data)
    }

    override fun createOffer() {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                webRtcScope.launch {
                    controllerEvent.emit(WebRtcEvent.Host.SetLocalSdp(observer, sdp))

                    controllerEvent.emit(WebRtcEvent.Host.SendSdpToGuest(sdp))
                }
            }
        )

        peerConnection?.createOffer(sdpObserver, constraints)
    }

    override fun createAnswer() {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                webRtcScope.launch {
                    controllerEvent.emit(WebRtcEvent.Guest.SetLocalSdp(observer, sdp))

                    controllerEvent.emit(WebRtcEvent.Guest.SendSdpToHost(sdp))
                }
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
                super.onConnectionChange(newState)
                webRtcScope.launch {
                    controllerEvent.emit(WebRtcEvent.StateChange.Connection(newState))
                }
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                webRtcScope.launch {
                    controllerEvent.emit(WebRtcEvent.StateChange.Signaling(p0))
                }
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                webRtcScope.launch {
                    controllerEvent.emit(WebRtcEvent.StateChange.IceConnection(p0))
                }
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                webRtcScope.launch {
                    controllerEvent.emit(WebRtcEvent.StateChange.IceGathering(p0))
                }
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let { ice ->
                    webRtcScope.launch {
                        if (isHost) {
                            controllerEvent.emit(WebRtcEvent.Host.SendIceToGuest(ice))
                        } else {
                            controllerEvent.emit(WebRtcEvent.Guest.SendIceToHost(ice))
                        }
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
