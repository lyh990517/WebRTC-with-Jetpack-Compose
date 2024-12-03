package com.example.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.call.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun OtherUserOnInputNotification() {
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

@Composable
fun NewMessageReceivedNotification(
    messages: List<ChatMessage>,
    scope: CoroutineScope,
    lazyListState: LazyListState,
    onClick: () -> Unit
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
            val message = messages[messages.size - 1]

            if (message is ChatMessage.TextMessage) {
                Text(
                    text = message.message,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = {
                scope.launch {
                    lazyListState.animateScrollToItem(messages.size - 1)
                }
                onClick()
            }) {
                Text("View", color = Color.White)
            }
        }
    }
}
