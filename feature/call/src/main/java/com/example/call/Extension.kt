package com.example.call

import com.example.call.ui.chat.ChatMessage
import com.example.webrtc.client.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun Flow<Message>.mapToChatMessage() = map { message ->
    when (message) {
        Message.InputEvent -> ChatMessage.InputEvent
        is Message.PlainString -> {
            ChatMessage.TextMessage(
                type = ChatMessage.ChatType.OTHER,
                message = message.data
            )
        }
    }
}

fun String.toChatMessage() = ChatMessage.TextMessage(
    type = ChatMessage.ChatType.ME,
    message = this
)
