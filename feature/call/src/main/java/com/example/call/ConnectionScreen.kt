package com.example.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
    onMessage: (Message) -> Unit
) {
    var message by remember { mutableStateOf("") }

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
                .align(Alignment.TopCenter),
            onToggleVoice = onToggleVoice,
            onToggleVideo = onToggleVideo,
            onDisconnect = onDisconnect,
        )
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White)
            ) {
                items(messages()) {
                    Text(it, color = Color.Red)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White),
                    value = message,
                    onValueChange = { message = it }
                )
                Button(
                    onClick = {
                        onMessage(Message.PlainString(message))
                        message = ""
                    }
                ) {
                    Text("send")
                }
            }
        }
    }
}