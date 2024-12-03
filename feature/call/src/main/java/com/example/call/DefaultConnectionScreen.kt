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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.call.ChatMessage
import com.example.call.isLoadingStatus
import com.example.call.mapToChatMessage
import com.example.call.toChatMessage
import com.example.webrtc.sdk.factory.WebRtcClientFactory
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer

@Composable
fun DefaultConnectionScreen(roomId: String) {
    val context = LocalContext.current
    val webRtcClient = remember { WebRtcClientFactory.create(context) }
    var isShowStatus by remember { mutableStateOf(true) }
    var otherUserOnInput by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var currentEvent by remember { mutableStateOf("") }
    var isChat by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        webRtcClient.connect(roomId)
        webRtcClient.startCapture()
    }

    LaunchedEffect(Unit) {
        webRtcClient
            .getMessages()
            .mapToChatMessage()
            .collect { chatMessage ->
                when (chatMessage) {
                    ChatMessage.InputEvent -> {
                        otherUserOnInput = true
                    }

                    is ChatMessage.TextMessage -> {
                        messages.add(chatMessage)
                    }
                }
            }
    }

    LaunchedEffect(Unit) {
        webRtcClient.getEvent().collect { webrtcEvent ->
            currentEvent = webrtcEvent.toString()

            if (webrtcEvent.isLoadingStatus()) {
                isShowStatus = false
            }
        }
    }


    LaunchedEffect(otherUserOnInput) {
        if (otherUserOnInput) {
            delay(800)
            otherUserOnInput = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        ConnectionContent(
            isChat = isChat,
            messages = messages,
            localSurfaceViewRenderer = webRtcClient.getLocalSurface(),
            remoteSurfaceViewRenderer = webRtcClient.getRemoteSurface(),
            otherUserOnInput = { otherUserOnInput },
            onEvent = { event ->
                when (event) {
                    ConnectionEvent.OnDisconnect -> webRtcClient.disconnect()
                    is ConnectionEvent.Input.OnInputStateChanged -> webRtcClient.sendInputEvent()
                    is ConnectionEvent.Input.OnMessage -> {
                        webRtcClient.sendMessage(event.message)
                        messages.add(event.message.toChatMessage(ChatMessage.ChatType.ME))
                    }

                    ConnectionEvent.OnToggleChat -> isChat = !isChat
                    ConnectionEvent.OnToggleVideo -> webRtcClient.toggleVideo()
                    ConnectionEvent.OnToggleVoice -> webRtcClient.toggleVoice()
                }
            }
        )
        if (isShowStatus) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = currentEvent,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ConnectionContent(
    messages: List<ChatMessage>,
    otherUserOnInput: () -> Boolean,
    isChat: Boolean,
    remoteSurfaceViewRenderer: SurfaceViewRenderer,
    localSurfaceViewRenderer: SurfaceViewRenderer,
    onEvent: (ConnectionEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        RemoteSurfaceViewRenderer(remoteSurfaceViewRenderer)

        LocalSurfaceViewRenderer(localSurfaceViewRenderer)

        Controller(
            onEvent = onEvent,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

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
                messages = messages,
                onEvent = onEvent,
                otherUserOnInput = otherUserOnInput
            )
        }
    }
}
