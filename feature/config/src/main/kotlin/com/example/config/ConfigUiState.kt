package com.example.config

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState


data class ConfigUiState(
    val accessCode: String = "",
    val eventSink: (ConfigUiEvent) -> Unit
) : CircuitUiState

sealed interface ConfigUiEvent : CircuitUiEvent {
    data class InputAccessCode(val code: String) : ConfigUiEvent
    sealed interface Route : ConfigUiEvent {
        data object CreateRoom : Route
        data object JoinRoom : Route
    }
}