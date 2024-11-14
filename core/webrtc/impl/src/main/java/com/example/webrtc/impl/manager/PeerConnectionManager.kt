package com.example.webrtc.impl.manager

import com.example.model.RemoteSurface
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
internal class PeerConnectionManager @Inject constructor(
    @RemoteSurface private val remoteSurface: SurfaceViewRenderer,
    private val fireStoreManager: FireStoreManager,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val localMediaStream: MediaStream,
) {
    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private lateinit var peerConnection: PeerConnection

    fun connectToPeer(isHost: Boolean, roomID: String) {
        peerConnectionFactory.createPeerConnection(
            iceServer,
            createPeerConnectionObserver(isHost, roomID)
        )?.let { connection ->
            peerConnection = connection
        }

        peerConnection.addStream(localMediaStream)
    }

    fun createOffer(roomID: String) {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                peerConnection.setLocalDescription(observer, sdp)

                fireStoreManager.sendSdpToRoom(sdp = sdp, roomId = roomID)
            }
        )

        peerConnection.createOffer(sdpObserver, constraints)
    }

    fun createAnswer(roomID: String) {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                peerConnection.setLocalDescription(observer, sdp)

                fireStoreManager.sendSdpToRoom(sdp = sdp, roomId = roomID)
            }
        )

        peerConnection.createAnswer(sdpObserver, constraints)
    }

    fun closeConnection() {
        peerConnection.close()
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

    private fun createPeerConnectionObserver(
        isHost: Boolean,
        roomID: String,
    ): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let {
                    fireStoreManager.sendIceCandidateToRoom(it, isHost, roomID)
                    peerConnection.addIceCandidate(it)
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
