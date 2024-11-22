package com.example.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.call.state.CallEvent
import com.example.call.state.CallState
import com.example.call.ui.ControllerUi
import com.example.call.ui.LocalSurface
import com.example.call.ui.RemoteSurface
import com.example.call.ui.chat.Chatting
import com.example.webrtc.client.event.WebRtcEvent
import kotlinx.coroutines.delay
import org.webrtc.DataChannel
import org.webrtc.PeerConnection

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val webrtcEvent by viewModel.webrtcEvents.collectAsState(WebRtcEvent.None)
    var isShowStatus by remember { mutableStateOf(true) }
    var otherUserOnInput by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { event ->
            when (event) {
                CallEvent.InputEvent -> {
                    otherUserOnInput = true
                }
            }
        }
    }

    LaunchedEffect(webrtcEvent) {
        val shouldHideStatus = when (webrtcEvent) {
            is WebRtcEvent.StateChange.DataChannel ->
                (webrtcEvent as WebRtcEvent.StateChange.DataChannel).state == DataChannel.State.OPEN

            is WebRtcEvent.StateChange.IceConnection ->
                (webrtcEvent as WebRtcEvent.StateChange.IceConnection).state == PeerConnection.IceConnectionState.CONNECTED

            is WebRtcEvent.StateChange.Signaling ->
                (webrtcEvent as WebRtcEvent.StateChange.Signaling).state == PeerConnection.SignalingState.STABLE

            else -> false
        }

        if (shouldHideStatus) {
            isShowStatus = false
        }
    }


    LaunchedEffect(otherUserOnInput) {
        if (otherUserOnInput) {
            delay(800)
            otherUserOnInput = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (state) {
            CallState.Loading -> {
                LoadingContent()
            }

            is CallState.Success -> {
                CallContent(
                    speechMode = viewModel.isSpeechMode,
                    state = state as CallState.Success,
                    otherUserOnInput = { otherUserOnInput },
                    onToggleVoice = viewModel::toggleVoice,
                    onToggleVideo = viewModel::toggleVideo,
                    onDisconnect = viewModel::disconnect,
                    onMessage = viewModel::sendMessage,
                    onInputChange = viewModel::sendInputEvent,
                    onSpeech = viewModel::startSpeech
                )
            }
        }
        if (isShowStatus) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = webrtcEvent.toString(),
                color = Color.White
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
    speechMode: Boolean,
    state: CallState.Success,
    otherUserOnInput: () -> Boolean,
    onSpeech: () -> Unit,
    onToggleVoice: () -> Unit,
    onToggleVideo: () -> Unit,
    onInputChange: () -> Unit,
    onDisconnect: () -> Unit,
    onMessage: (String) -> Unit,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                onToggleVoice = onToggleVoice,
                onToggleVideo = onToggleVideo,
                onToggleChat = { isChat = !isChat },
                onDisconnect = onDisconnect,
                onSpeech = onSpeech,
                speechMode = speechMode
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
                otherUserOnInput = otherUserOnInput
            )
        }
    }
}
