package com.example.webrtc.impl.manager

import org.webrtc.AudioTrack
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject

internal class ResourceManager @Inject constructor(
    private val localVideoTrack: VideoTrack,
    private val localAudioTrack: AudioTrack,
    private val videoCapturer: VideoCapturer,
) {
    fun toggleVoice() {
        localAudioTrack.setEnabled(!localAudioTrack.enabled())
    }

    fun toggleVideo() {
        localVideoTrack.setEnabled(!localVideoTrack.enabled())
    }

    fun startCapture() {
        videoCapturer.startCapture(1920, 1080, 60)
    }

    fun dispose() {
        localAudioTrack.dispose()
        localVideoTrack.dispose()
        videoCapturer.dispose()
    }
}