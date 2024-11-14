package com.example.connect

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState


data class ConnectUiState(
    val eventSink: (ConnectUiEvent) -> Unit = {}
) : CircuitUiState

sealed interface ConnectUiEvent : CircuitUiEvent {
    data class InputAccessCode(val code: String) : ConnectUiEvent

    sealed interface Route : ConnectUiEvent {
        data object CreateRoom : Route
        data object JoinRoom : Route
    }
}