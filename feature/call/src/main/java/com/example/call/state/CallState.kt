package com.example.call.state

import org.webrtc.SurfaceViewRenderer

sealed interface CallState {
    data object Loading : CallState

    data class Success(
        val local: SurfaceViewRenderer,
        val remote: SurfaceViewRenderer
    ) : CallState
}