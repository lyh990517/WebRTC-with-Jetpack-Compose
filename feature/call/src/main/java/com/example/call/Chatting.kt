package com.example.call

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
import com.example.call.ChatList
import com.example.call.ChatMessage
import com.example.call.ChatMessageInput
import kotlinx.coroutines.launch

@Composable
fun Chatting(
    messages: List<ChatMessage>,
    otherUserOnInput: () -> Boolean,
    onEvent: (ConnectionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    var showNewMessageNotification by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler {
        onEvent(ConnectionEvent.OnToggleChat)
    }

    LaunchedEffect(messages.size) {
        if (lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
            showNewMessageNotification =
                lastVisibleItem != null
                        && lastVisibleItem.index < messages.size - 1
        }

        val isAtBottom =
            lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == messages.size - 1

        if (isAtBottom) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            ChattingTopBar(onEvent)

            ChatList(
                lazyListState = lazyListState,
                messages = messages,
                modifier = Modifier.weight(1f)
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
                        messages = messages,
                        scope = scope,
                        lazyListState = lazyListState,
                        onClick = { showNewMessageNotification = false }
                    )
                }
                ChatMessageInput(
                    onEvent = { event ->
                        when (event) {
                            is ConnectionEvent.Input.OnInputStateChanged -> onEvent(event)
                            is ConnectionEvent.Input.OnMessage -> {
                                if (event.message.isNotBlank()) {
                                    scope.launch {
                                        onEvent(ConnectionEvent.Input.OnMessage(event.message))
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
