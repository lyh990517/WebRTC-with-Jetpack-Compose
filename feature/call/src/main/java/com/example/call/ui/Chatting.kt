package com.example.call.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.example.call.state.CallState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface ChatMessage {
    val type: ChatType

    data class Message(
        override val type: ChatType,
        val message: String,
    ) : ChatMessage

    data class Image(
        override val type: ChatType,
        val images: List<ImageBitmap>,
    ) : ChatMessage

    data class File(
        override val type: ChatType,
        val files: List<java.io.File>,
    ) : ChatMessage

    enum class ChatType {
        ME,
        OTHER
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chatting(
    state: CallState.Success,
    onMessage: (ChatMessage) -> Unit,
    onInputChange: () -> Unit,
    onInputStopped: () -> Unit,
    onToggleChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    var showNewMessageNotification by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.otherUserOnInput) {
        if (state.otherUserOnInput) {
            delay(800)
            onInputStopped()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            TopAppBar(
                title = { Text("Chatting", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onToggleChat) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            ChatList(lazyListState = lazyListState, state = state)

            LaunchedEffect(state.messages.size) {
                if (lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                    val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
                    showNewMessageNotification =
                        lastVisibleItem != null
                                && lastVisibleItem.index < state.messages.size - 1
                }
            }

            Column {
                AnimatedVisibility(
                    visible = state.otherUserOnInput,
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
                NewChatNotification(
                    showNewMessageNotification = showNewMessageNotification,
                    state = state,
                    scope = scope,
                    lazyListState = lazyListState
                ) {
                    showNewMessageNotification = false
                }
                MessageInputUi(
                    onMessage = { message ->
                        when (message) {
                            is ChatMessage.File -> {

                            }

                            is ChatMessage.Image -> {
                                scope.launch {
                                    val isAtBottom =
                                        lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.messages.size - 1
                                    onMessage(message)

                                    if (isAtBottom) {
                                        lazyListState.animateScrollToItem(state.messages.size - 1)
                                    }
                                }
                            }

                            is ChatMessage.Message -> {
                                if (message.message.isNotBlank()) {
                                    scope.launch {
                                        val isAtBottom =
                                            lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.messages.size - 1
                                        onMessage(message)

                                        if (isAtBottom) {
                                            lazyListState.animateScrollToItem(state.messages.size - 1)
                                        }
                                    }
                                }
                            }
                        }
                    }, onInputChange = onInputChange
                )
            }
        }
    }
}
