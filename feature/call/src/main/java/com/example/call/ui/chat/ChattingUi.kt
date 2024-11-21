package com.example.call.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.call.state.CallState
import kotlinx.coroutines.launch

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

@Composable
fun Chatting(
    state: CallState.Success,
    otherUserOnInput: () -> Boolean,
    onMessage: (String) -> Unit,
    onInputChange: () -> Unit,
    onToggleChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    var showNewMessageNotification by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler {
        onToggleChat()
    }

    LaunchedEffect(state.messages.size) {
        if (lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
            showNewMessageNotification =
                lastVisibleItem != null
                        && lastVisibleItem.index < state.messages.size - 1
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            ChattingTopBar(onToggleChat)

            ChatList(
                lazyListState = lazyListState,
                state = state
            )

            Column {
                AnimatedVisibility(
                    visible = otherUserOnInput(),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                ) {
                    OtherUserOnInputNotification()
                }
                AnimatedVisibility(
                    visible = showNewMessageNotification,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                ) {
                    NewMessageReceivedNotification(
                        state = state,
                        scope = scope,
                        lazyListState = lazyListState,
                        onClick = { showNewMessageNotification = false }
                    )
                }
                ChatMessageInput(
                    onMessage = {
                        if (it.isNotBlank()) {
                            scope.launch {
                                val isAtBottom =
                                    lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.messages.size - 1
                                onMessage(it)

                                if (isAtBottom) {
                                    lazyListState.animateScrollToItem(state.messages.size - 1)
                                }
                            }
                        }
                    }, onInputChange = onInputChange
                )
            }
        }
    }
}