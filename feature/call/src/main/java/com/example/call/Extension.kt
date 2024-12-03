package com.example.call

import com.example.webrtc.sdk.event.WebRtcEvent
import com.example.webrtc.sdk.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.webrtc.DataChannel
import org.webrtc.PeerConnection

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

fun String.toChatMessage(type: ChatMessage.ChatType) = ChatMessage.TextMessage(
    type = type,
    message = this
)

fun WebRtcEvent.isLoadingStatus() = when (this) {
    is WebRtcEvent.StateChange.DataChannel ->
        state == DataChannel.State.OPEN

    is WebRtcEvent.StateChange.IceConnection ->
        state == PeerConnection.IceConnectionState.CONNECTED

    is WebRtcEvent.StateChange.Signaling ->
        state == PeerConnection.SignalingState.STABLE

    else -> false
}
