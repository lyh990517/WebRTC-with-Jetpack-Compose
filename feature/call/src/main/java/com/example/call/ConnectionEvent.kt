package com.example.call

sealed interface ConnectionEvent {
    data object OnToggleVideo : ConnectionEvent
    data object OnToggleVoice : ConnectionEvent
    data object OnDisconnect : ConnectionEvent
    data object OnToggleChat : ConnectionEvent

    sealed interface Input : ConnectionEvent {
        data class OnInputStateChanged(val input: String) : Input
        data class OnMessage(val message: String) : Input
    }
}
