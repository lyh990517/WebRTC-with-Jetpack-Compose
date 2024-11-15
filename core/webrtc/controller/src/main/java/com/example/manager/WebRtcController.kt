package com.example.manager

import com.example.event.EventBus.eventFlow
import com.example.event.WebRtcEvent
import com.example.model.RemoteSurface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcController @Inject constructor(
    private val webRtcScope: CoroutineScope,
    @RemoteSurface private val remoteSurface: SurfaceViewRenderer,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val localMediaStream: MediaStream,
) {
    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private lateinit var peerConnection: PeerConnection

    fun connectToPeerAsHost(roomID: String) {
        peerConnectionFactory.createPeerConnection(
            iceServer,
            createPeerConnectionHostObserver(roomID)
        )?.let { connection ->
            peerConnection = connection
        }

        peerConnection.addStream(localMediaStream)
    }

    fun connectToPeerAsGuest(roomID: String) {
        peerConnectionFactory.createPeerConnection(
            iceServer,
            createPeerConnectionGuestObserver(roomID)
        )?.let { connection ->
            peerConnection = connection
        }

        peerConnection.addStream(localMediaStream)
    }

    fun createOffer(roomID: String) {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                webRtcScope.launch {
                    eventFlow.emit(WebRtcEvent.Host.SetLocalSdp(observer, sdp))
                    eventFlow.emit(WebRtcEvent.Host.SendSdpToGuest(sdp = sdp, roomId = roomID))
                }
            }
        )

        peerConnection.createOffer(sdpObserver, constraints)
    }

    fun createAnswer(roomID: String) {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                webRtcScope.launch {
                    eventFlow.emit(WebRtcEvent.Guest.SetLocalSdp(observer, sdp))
                    eventFlow.emit(WebRtcEvent.Guest.SendSdpToHost(sdp = sdp, roomId = roomID))
                }
            }
        )

        peerConnection.createAnswer(sdpObserver, constraints)
    }

    fun closeConnection() {
        peerConnection.close()
    }

    fun setLocalDescription(sdp: SessionDescription, observer: SdpObserver) {
        peerConnection.setLocalDescription(observer, sdp)
    }

    fun setRemoteDescription(sdp: SessionDescription) {
        val sdpObserver = createSdpObserver()

        peerConnection.setRemoteDescription(sdpObserver, sdp)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection.addIceCandidate(iceCandidate)
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

    private fun createPeerConnectionHostObserver(
        roomID: String,
    ): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let {
                    webRtcScope.launch {
                        eventFlow.emit(WebRtcEvent.Host.SendIceToGuest(ice = it, roomId = roomID))
                        eventFlow.emit(WebRtcEvent.Host.SetLocalIce(ice = it))
                    }
                }
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {
                p0?.let { mediaStream ->
                    mediaStream.videoTracks?.get(0)?.addSink(remoteSurface)
                }
            }

            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        }

    private fun createPeerConnectionGuestObserver(
        roomID: String,
    ): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let {
                    webRtcScope.launch {
                        eventFlow.emit(WebRtcEvent.Guest.SendIceToHost(ice = it, roomId = roomID))
                        eventFlow.emit(WebRtcEvent.Guest.SetLocalIce(ice = it))
                    }
                }
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {
                p0?.let { mediaStream ->
                    mediaStream.videoTracks?.get(0)?.addSink(remoteSurface)
                }
            }

            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        }


    companion object {
        private const val ICE_SERVER_URL = "stun:stun4.l.google.com:19302"
    }
}
