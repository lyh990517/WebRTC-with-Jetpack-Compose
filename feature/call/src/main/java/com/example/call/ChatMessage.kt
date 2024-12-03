package com.example.call

sealed interface ChatMessage {
    data class TextMessage(
        val type: ChatType,
        val message: String
    ) : ChatMessage

    data object InputEvent : ChatMessage

    enum class ChatType {
        ME,
        OTHER
    }
}
