package com.example.webrtc.client.model

sealed interface Message {
    data class PlainString(val data: String) : Message
    data object InputEvent : Message
}