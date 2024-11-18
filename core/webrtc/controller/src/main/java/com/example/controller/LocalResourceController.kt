package com.example.controller

import com.example.model.LocalSurface
import com.example.model.RemoteSurface
import com.example.webrtc.client.Controller
import org.webrtc.AudioTrack
import org.webrtc.MediaStream
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LocalResourceController @Inject constructor(
    @LocalSurface private val localSurface: SurfaceViewRenderer,
    @RemoteSurface private val remoteSurface: SurfaceViewRenderer,
    private val localVideoTrack: VideoTrack,
    private val localAudioTrack: AudioTrack,
    private val videoCapturer: VideoCapturer
) : Controller.LocalResource {
    override fun getLocalSurface(): SurfaceViewRenderer = localSurface

    override fun getRemoteSurface(): SurfaceViewRenderer = remoteSurface

    override fun getVideoTrack(): VideoTrack = localVideoTrack

    override fun getAudioTrack(): AudioTrack = localAudioTrack

    override fun sinkToRemoteSurface(remoteMediaStream: MediaStream) {
        remoteMediaStream.videoTracks?.get(0)?.addSink(remoteSurface)
    }

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