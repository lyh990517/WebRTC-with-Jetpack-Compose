package com.example.home

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState


data class HomeUiState(
    val roomId: String,
    val eventSink: suspend (HomeEvent) -> Unit = {}
) : CircuitUiState

sealed interface HomeEvent : CircuitUiEvent {
    data class Input(val roomId: String) : HomeEvent
    data class Connect(val isHost: Boolean) : HomeEvent
}