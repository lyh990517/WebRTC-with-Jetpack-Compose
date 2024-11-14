package com.example.webrtc.impl

import com.example.webrtc.api.WebRtcClient
import com.example.webrtc.impl.manager.FireStoreManager
import com.example.webrtc.impl.manager.PeerConnectionManager
import com.example.webrtc.impl.manager.SignalingManager
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import org.webrtc.AudioTrack
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WebRtcClientImpl @Inject constructor(
    private val signalingManager: SignalingManager,
    private val peerConnectionManager: PeerConnectionManager,
    private val fireStoreManager: FireStoreManager,
    private val localAudioTrack: AudioTrack,
    private val localVideoTrack: VideoTrack,
    private val videoCapturer: VideoCapturer,
) : WebRtcClient {
    override fun connect(roomID: String, isHost: Boolean) {
        peerConnectionManager.connectToPeer(isHost, roomID)

        videoCapturer.startCapture(320, 240, 60)

        signalingManager.observeSignaling(isHost, roomID)
    }

    override fun getRoomInformation(roomID: String): Flow<DocumentSnapshot> =
        fireStoreManager.getRoomInfo(roomID)

    override fun toggleVoice() {
        localAudioTrack.setEnabled(!localAudioTrack.enabled())
    }

    override fun toggleVideo() {
        localVideoTrack.setEnabled(!localVideoTrack.enabled())
    }

    override fun disconnect() {
        localAudioTrack.dispose()
        localVideoTrack.dispose()
        peerConnectionManager.closeConnection()
        signalingManager.close()
    }
}
