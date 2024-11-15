package com.example.controller

import com.example.webrtc.client.Controller
import org.webrtc.AudioTrack
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LocalResourceController @Inject constructor(
    private val localVideoTrack: VideoTrack,
    private val localAudioTrack: AudioTrack,
    private val videoCapturer: VideoCapturer,
) : Controller.LocalResource {
    override fun toggleVoice() {
        localAudioTrack.setEnabled(!localAudioTrack.enabled())
    }

    override fun toggleVideo() {
        localVideoTrack.setEnabled(!localVideoTrack.enabled())
    }

    override fun startCapture() {
        videoCapturer.startCapture(1920, 1080, 60)
    }

    override fun dispose() {
        localAudioTrack.dispose()
        localVideoTrack.dispose()
        videoCapturer.dispose()
    }
}