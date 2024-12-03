package com.example.webrtc.sdk.controller

import org.webrtc.AudioTrack
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack

internal class LocalResourceManager(
    private val localSurface: SurfaceViewRenderer,
    private val remoteSurface: SurfaceViewRenderer,
    private val localVideoTrack: VideoTrack,
    private val localAudioTrack: AudioTrack,
    private val videoCapturer: VideoCapturer
) {
    fun getLocalSurface(): SurfaceViewRenderer = localSurface

    fun getRemoteSurface(): SurfaceViewRenderer = remoteSurface

    fun getVideoTrack(): VideoTrack = localVideoTrack

    fun getAudioTrack(): AudioTrack = localAudioTrack

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
        videoCapturer.stopCapture()
        localAudioTrack.dispose()
        localVideoTrack.dispose()
        videoCapturer.dispose()
    }
}
