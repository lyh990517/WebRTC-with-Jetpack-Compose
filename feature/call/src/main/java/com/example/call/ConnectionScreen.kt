package com.example.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.call.state.CallState
import com.example.call.ui.ChatMessage
import com.example.call.ui.Chatting
import com.example.call.ui.ControllerUi
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetch()
        delay(200)
        viewModel.connect()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.disconnect()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.disconnect()
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
                onInputChange = viewModel::onInputChange,
                onInputStopped = viewModel::onInputStopped
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
    state: CallState.Success,
    onToggleVoice: () -> Unit,
    onToggleVideo: () -> Unit,
    onInputChange: () -> Unit,
    onInputStopped: () -> Unit,
    onDisconnect: () -> Unit,
    onMessage: (ChatMessage) -> Unit,
) {
    var isChat by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        RemoteSurface(state.remote)
        LocalSurface(state.local)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(vertical = 16.dp, horizontal = 32.dp)
        ) {
            ControllerUi(
                modifier = Modifier.fillMaxWidth(),
                onToggleVoice = onToggleVoice,
                onToggleVideo = onToggleVideo,
                onToggleChat = { isChat = !isChat },
                onDisconnect = onDisconnect,
            )
        }

        AnimatedVisibility(
            visible = isChat,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Chatting(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ),
                state = state,
                onMessage = onMessage,
                onToggleChat = { isChat = !isChat },
                onInputChange = onInputChange,
                onInputStopped = onInputStopped
            )
        }
    }
}

@Composable
private fun RemoteSurface(remoteSurface: SurfaceViewRenderer) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { remoteSurface }
    )
}

@Composable
private fun LocalSurface(
    localSurface: SurfaceViewRenderer,
) {
    var localViewOffset by remember { mutableStateOf(Offset(16f, 16f)) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(100.dp)
                .offset { IntOffset(localViewOffset.x.toInt(), localViewOffset.y.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        localViewOffset += dragAmount
                    }
                }
                .clip(CircleShape),
            factory = { localSurface }
        )
    }
}
