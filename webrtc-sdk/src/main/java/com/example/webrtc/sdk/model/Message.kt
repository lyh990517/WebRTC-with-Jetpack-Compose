package com.example.webrtc.sdk.model

sealed interface Message {
    data class PlainString(val data: String) : Message
    data object InputEvent : Message
}
