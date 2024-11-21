package com.example.call.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.call.state.CallState
import kotlinx.coroutines.launch

sealed interface ChatMessage {
    data class TextMessage(
        val type: ChatType,
        val message: String? = null,
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "other user on input",
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = showNewMessageNotification,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val message = state.messages[state.messages.size - 1]

                            if (message is ChatMessage.TextMessage) {
                                Text(
                                    text = message.message ?: "",
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            TextButton(onClick = {
                                scope.launch {
                                    lazyListState.animateScrollToItem(state.messages.size - 1)
                                }
                                showNewMessageNotification = false
                            }) {
                                Text("View", color = Color.White)
                            }
                        }
                    }
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