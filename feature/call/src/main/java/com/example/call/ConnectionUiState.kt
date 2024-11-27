package com.example.call

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.webrtc.SurfaceViewRenderer

data class ConnectionUiState(
    val local: SurfaceViewRenderer,
    val remote: SurfaceViewRenderer,
    val eventSink: (ConnectionEvent) -> Unit = {}
) : CircuitUiState

sealed interface ConnectionEvent : CircuitUiEvent {
    data object Connect : ConnectionEvent
    data object DisConnect : ConnectionEvent
    data object ToggleVoice : ConnectionEvent
    data object ToggleVideo : ConnectionEvent
}