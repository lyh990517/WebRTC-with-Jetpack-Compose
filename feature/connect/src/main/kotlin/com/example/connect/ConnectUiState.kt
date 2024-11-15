package com.example.connect

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.webrtc.SurfaceViewRenderer


data class ConnectUiState(
    val localSurfaceView: SurfaceViewRenderer? = null,
    val remoteSurfaceView: SurfaceViewRenderer? = null,
    val eventSink: (ConnectUiEvent) -> Unit = {}
) : CircuitUiState

sealed interface ConnectUiEvent : CircuitUiEvent