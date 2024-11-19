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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val messages = remember { mutableStateListOf<String>() }

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
                    messages.add(message.data)
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
                onMessage = viewModel::sendMessage,
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
    messages: () -> List<String>,
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

@Composable
private fun Chatting(
    messages: () -> List<String>,
    onMessage: (String) -> Unit,
    onToggleChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }

    Box {
        Column(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages()) { message ->
                    val isSent = message.startsWith("Me:")
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Text(
                            text = message.removePrefix("Me: "),
                            modifier = Modifier
                                .background(
                                    color = if (isSent) Color(0xFFDCF8C6) else Color.White,
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isSent) 12.dp else 0.dp,
                                        bottomEnd = if (isSent) 0.dp else 12.dp
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .widthIn(max = 250.dp),
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Green)
                    .height(70.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    value = message,
                    onValueChange = { message = it },
                    decorationBox = { innerTextField ->
                        if (message.isEmpty()) {
                            Text(
                                text = "Type a message...",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerTextField()
                    },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    )
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
        Button(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp),
            onClick = onToggleChat
        ) {
            Text("back")
        }
    }
}