package com.example.call.state

import com.example.call.ui.ChatMessage
import org.webrtc.SurfaceViewRenderer

sealed interface CallState {
    data object Loading : CallState

    data class Success(
        val local: SurfaceViewRenderer,
        val remote: SurfaceViewRenderer,
        val messages: List<ChatMessage>
    ) : CallState
}

sealed interface CallEvent {
    data object InputEvent : CallEvent
}