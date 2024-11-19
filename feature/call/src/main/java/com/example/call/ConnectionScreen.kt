package com.example.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.call.state.CallState
import com.example.call.ui.ControllerUi
import com.example.call.viewmodel.ConnectionViewModel
import com.example.webrtc.client.model.Message
import kotlinx.coroutines.delay

const val connectionRoute = "connection"
const val roomIdArg = "roomId"

fun NavHostController.navigateToConnection(
    roomId: String
) {
    val route = "$connectionRoute?$roomIdArg=$roomId"

    navigate(route)
}

fun NavGraphBuilder.connectionScreen() {
    composable(
        route = "$connectionRoute?$roomIdArg={$roomIdArg}",
        arguments =
        listOf(
            navArgument(roomIdArg) {
                nullable = false
                type = NavType.StringType
            }
        )
    ) {
        ConnectionScreen()
    }
}

data class ChatMessage(
    val type: String,
    val message: String
)

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val messages = remember { mutableStateListOf<ChatMessage>() }

    LaunchedEffect(Unit) {
        viewModel.fetch()
        delay(200)
        viewModel.connect()
    }

    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            when (message) {
                is Message.File -> {}
                is Message.PlainString -> {
                    messages.add(ChatMessage("other", message.data))
                }
            }
        }
    }

    when (state) {
        CallState.Loading -> {
            LoadingContent()
        }

        is CallState.Success -> {
            CallContent(
                state = state as CallState.Success,
                onToggleVoice = viewModel::toggleVoice,
                onToggleVideo = viewModel::toggleVideo,
                onDisconnect = viewModel::disconnect,
                onMessage = {
                    messages.add(ChatMessage("me", it))
                    viewModel.sendMessage(it)
                },
                messages = { messages.toList() }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@Composable
private fun CallContent(
    messages: () -> List<ChatMessage>,
    state: CallState.Success,
    onToggleVoice: () -> Unit,
    onToggleVideo: () -> Unit,
    onDisconnect: () -> Unit,
    onMessage: (String) -> Unit
) {
    var isChat by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { state.remote }
        )
        AndroidView(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(0.5f)
                .align(Alignment.BottomStart),
            factory = { state.local }
        )
        ControllerUi(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            onToggleVoice = onToggleVoice,
            onToggleVideo = onToggleVideo,
            onToggleChat = { isChat = !isChat },
            onDisconnect = onDisconnect,
        )
        if (isChat) {
            Chatting(
                messages = messages,
                onMessage = onMessage,
                onToggleChat = { isChat = !isChat }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Chatting(
    messages: () -> List<ChatMessage>,
    onMessage: (String) -> Unit,
    onToggleChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            TopAppBar(
                title = { Text("Chatting") },
                navigationIcon = {
                    IconButton(onClick = onToggleChat) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages()) { chatMessage ->
                    val isSent = chatMessage.type == "me"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSent) Color(0xFF9CE09F) else Color(
                                        0xFF4692E1
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
                                color = if (isSent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 메시지 입력 필드
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
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onMessage(message)
                        message = ""
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Send")
                }
            }
        }
    }
}
