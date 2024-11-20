package com.example.call.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.call.state.CallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.NewChatNotification(
    showNewMessageNotification: Boolean,
    state: CallState.Success,
    scope: CoroutineScope,
    lazyListState: LazyListState,
    onClick: () -> Unit
) {
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
                val lastChat = state.messages[state.messages.size - 1]

                when (lastChat) {
                    is ChatMessage.File -> {
                        Text(
                            text = "File Received",
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            scope.launch {
                                lazyListState.animateScrollToItem(state.messages.size - 1)
                            }
                            onClick()
                        }) {
                            Text("View", color = Color.White)
                        }
                    }

                    is ChatMessage.Image -> {
                        Text(
                            text = "Image Received",
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            scope.launch {
                                lazyListState.animateScrollToItem(state.messages.size - 1)
                            }
                            onClick()
                        }) {
                            Text("View", color = Color.White)
                        }
                    }

                    is ChatMessage.Message -> {
                        Text(
                            text = lastChat.message,
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            scope.launch {
                                lazyListState.animateScrollToItem(state.messages.size - 1)
                            }
                            onClick()
                        }) {
                            Text("View", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}