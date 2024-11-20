package com.example.call.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChatItem(chatMessage: ChatMessage) {
    val isSent = chatMessage.type == ChatMessage.ChatType.ME

    when (chatMessage) {
        is ChatMessage.File -> {

        }

        is ChatMessage.Image -> {
            ImageChat(isSent, chatMessage)
        }

        is ChatMessage.Message -> {
            TextChat(isSent, chatMessage)
        }
    }
}

@Composable
private fun TextChat(
    isSent: Boolean,
    chatMessage: ChatMessage.Message
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isSent) listOf(
                            Color(0xFF9CE09F), Color(0xFF60BE7B)
                        ) else listOf(
                            Color(0xFF4692E1), Color(0xFF1C73D1)
                        )
                    ),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isSent) 12.dp else 0.dp,
                        bottomEnd = if (isSent) 0.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 250.dp)
        ) {
            Text(
                text = chatMessage.message,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ImageChat(isSent: Boolean, chatMessage: ChatMessage.Image) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isSent) listOf(
                            Color(0xFF9CE09F), Color(0xFF60BE7B)
                        ) else listOf(
                            Color(0xFF4692E1), Color(0xFF1C73D1)
                        )
                    ),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isSent) 12.dp else 0.dp,
                        bottomEnd = if (isSent) 0.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 250.dp)
        ) {
            Column {
                chatMessage.images.forEach {
                    Image(
                        bitmap = it,
                        contentDescription = "",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}
