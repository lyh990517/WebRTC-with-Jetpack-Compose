package com.example.webrtc.impl

import com.example.webrtc.api.WebRtcClient
import org.webrtc.AudioTrack
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WebRtcClientImpl @Inject constructor(
    private val signalingManager: SignalingManager,
    private val peerConnectionManager: PeerConnectionManager,
    private val localAudioTrack: AudioTrack,
    private val localVideoTrack: VideoTrack,
    private val videoCapturer: VideoCapturer,
) : WebRtcClient {
    override fun connect(roomID: String, isHost: Boolean) {
        peerConnectionManager.connectToPeer(isHost, roomID)

        peerConnectionManager.addStream()

        videoCapturer.startCapture(320, 240, 60)

        signalingManager.observeSignaling(isHost, roomID)
    }

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
