package com.example.webrtc.sdk.controller

import com.example.webrtc.sdk.event.WebRtcEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.webrtc.AudioTrack
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

class PeerConnectionManager(
    private val peerConnectionFactory: PeerConnectionFactory
) {
    private val peerConnectionEvent = MutableSharedFlow<WebRtcEvent>(extraBufferCapacity = 100)
    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }

    private var peerConnection: PeerConnection? = null

    fun initialize(isHost: Boolean) {
        peerConnection = peerConnectionFactory.createPeerConnection(isHost)
    }

    fun getEvent() = peerConnectionEvent.asSharedFlow()

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

    fun createOffer() {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                peerConnection?.setLocalDescription(observer, sdp)

                peerConnectionEvent.tryEmit(WebRtcEvent.Host.SendSdpToGuest(sdp))
            }
        )

        peerConnection?.createOffer(sdpObserver, constraints)
    }

    fun createAnswer() {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                peerConnection?.setLocalDescription(observer, sdp)

                peerConnectionEvent.tryEmit(WebRtcEvent.Guest.SendSdpToHost(sdp))
            }
        )

        peerConnection?.createAnswer(sdpObserver, constraints)
    }

    fun setRemoteDescription(sdp: SessionDescription) {
        val sdpObserver = createSdpObserver()

        peerConnection?.setRemoteDescription(sdpObserver, sdp)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun closeConnection() {
        peerConnection?.close()
    }

    fun initializeLocalResource(
        videoTrack: VideoTrack,
        audioTrack: AudioTrack
    ) {
        peerConnection?.addTrack(videoTrack)
        peerConnection?.addTrack(audioTrack)
    }

    fun createDataChannel(): DataChannel? {
        return peerConnection?.createDataChannel(
            "channel",
            DataChannel.Init().apply { negotiated = true }
        )
    }

    private fun createPeerConnectionObserver(
        isHost: Boolean
    ): Observer =
        object : Observer {
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                peerConnectionEvent.tryEmit(WebRtcEvent.StateChange.Connection(newState))
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                peerConnectionEvent.tryEmit(WebRtcEvent.StateChange.Signaling(p0))
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                peerConnectionEvent.tryEmit(WebRtcEvent.StateChange.IceConnection(p0))
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                peerConnectionEvent.tryEmit(WebRtcEvent.StateChange.IceGathering(p0))
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let { ice ->
                    if (isHost) {
                        peerConnectionEvent.tryEmit(WebRtcEvent.Host.SendIceToGuest(ice))
                    } else {
                        peerConnectionEvent.tryEmit(WebRtcEvent.Guest.SendIceToHost(ice))
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
                    if (isHost) {
                        peerConnectionEvent.tryEmit(WebRtcEvent.Host.VideoTrackReceived(track))
                    } else {
                        peerConnectionEvent.tryEmit(WebRtcEvent.Guest.VideoTrackReceived(track))
                    }
                }
            }
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

    companion object {
        private const val ICE_SERVER_URL = "stun:stun4.l.google.com:19302"
    }
}
