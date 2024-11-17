package com.example.webrtc.api

import org.webrtc.SurfaceViewRenderer

interface WebRtcClient {
    fun connect(roomID: String)

    fun disconnect()

    fun toggleVoice()

    fun toggleVideo()

    fun getLocalSurface(): SurfaceViewRenderer

    fun getRemoteSurface(): SurfaceViewRenderer
}
