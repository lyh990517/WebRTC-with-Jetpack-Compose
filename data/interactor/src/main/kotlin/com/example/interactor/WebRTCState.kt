package com.example.interactor

import org.webrtc.SurfaceViewRenderer

data class WebRTCState(
    val accessCode : String = "",
    val localSurfaceView: SurfaceViewRenderer? = null,
    val remoteSurfaceView: SurfaceViewRenderer? = null,
    val isAudioEnabled: Boolean = false,
    val isVideoEnabled: Boolean = false,
    val isJoin : Boolean = false
)
