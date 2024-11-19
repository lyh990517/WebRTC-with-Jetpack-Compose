package com.example.call.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.call.state.CallState
import kotlinx.coroutines.delay

data class ChatMessage(
    val type: ChatType,
    val message: String
) {
    enum class ChatType {
        ME,
        OTHER
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chatting(
    state: CallState.Success,
    onMessage: (String) -> Unit,
    onToggleChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }

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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages) { chatMessage ->
                    val isSent = chatMessage.type == ChatMessage.ChatType.ME
                    var receiveAnimation by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(50)
                        receiveAnimation = true
                    }

                    AnimatedVisibility(
                        visible = receiveAnimation,
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { if (isSent) 300 else -300 }),
                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { if (isSent) 300 else -300 })
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
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            onMessage(message)
                            message = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp),
                    elevation = ButtonDefaults.buttonElevation(6.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send", color = Color.White)
                }
            }
        }
    }
}
