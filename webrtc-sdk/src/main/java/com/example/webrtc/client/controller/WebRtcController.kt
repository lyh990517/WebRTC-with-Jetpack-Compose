package com.example.webrtc.client.controller

import com.example.webrtc.client.event.WebRtcEvent
import com.example.webrtc.client.model.Message
import com.example.webrtc.client.stts.SpeechRecognitionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

// TODO Turn 서버 지정

@Singleton
internal class WebRtcController @Inject constructor(
    private val webRtcScope: CoroutineScope,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val localResourceController: Controller.LocalResource,
    private val dataChannelManager: DataChannelManager,
    private val speechRecognitionManager: SpeechRecognitionManager
) : Controller.WebRtc {
    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }
    private val controllerEvent = MutableSharedFlow<WebRtcEvent>(extraBufferCapacity = 100)

    private var peerConnection: PeerConnection? = null

    override fun connect(roomID: String, isHost: Boolean, speechModeOn: Boolean) {
        peerConnection = peerConnectionFactory.createPeerConnection(isHost)

        peerConnection?.createDataChannel()

        webRtcScope.launch {
            withContext(Dispatchers.Main) {
                speechRecognitionManager.getResult().collect(::sendMessage)
            }
        }

        peerConnection?.initializeLocalResource(speechModeOn)

        if (isHost) {
            controllerEvent.tryEmit(WebRtcEvent.Host.SendOffer)
        }
    }

    override fun sendMessage(message: String) {
        dataChannelManager.sendMessage(message)
    }

    override fun sendInputEvent() {
        dataChannelManager.sendInputEvent()
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

    private fun PeerConnection.initializeLocalResource(speechModeOn: Boolean) {
        addTrack(localResourceController.getVideoTrack())
        if (!speechModeOn) addTrack(localResourceController.getAudioTrack())
    }

    private fun PeerConnectionFactory.createPeerConnection(
        isHost: Boolean
    ): PeerConnection? {
        val rtcConfig = createRtcConfig()
        val peerConnectionObserver = createPeerConnectionObserver(isHost)

        return createPeerConnection(
            rtcConfig,
            peerConnectionObserver
        )
    }

    private fun PeerConnection.createDataChannel() = dataChannelManager.initialize(this)

    private fun createRtcConfig() = RTCConfiguration(iceServer).apply {
        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        enableRtpDataChannel = true
    }

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
    ): Observer =
        object : Observer {
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
