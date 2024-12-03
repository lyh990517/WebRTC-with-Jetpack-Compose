package com.example.webrtc.sdk

import com.example.webrtc.sdk.api.WebRtcClient
import com.example.webrtc.sdk.controller.WebRtcController
import com.example.webrtc.sdk.event.EventHandler
import com.example.webrtc.sdk.event.WebRtcEvent
import com.example.webrtc.sdk.model.Message
import com.example.webrtc.sdk.signaling.Signaling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer

internal class WebRtcClientImpl(
    private val webRtcScope: CoroutineScope,
    private val eventHandler: EventHandler,
    private val webRtcController: WebRtcController,
    private val signaling: Signaling,
) : WebRtcClient {
    override fun connect(roomID: String) {
        webRtcScope.launch {
            val isHost = !signaling.getRoomExists(roomID)

            launch { eventHandler.start() }

            launch { signaling.start(roomID, isHost) }

            webRtcController.initialize(isHost)
        }
    }

    override fun startCapture() {
        webRtcController.startCapture()
    }

    override fun getEvent(): Flow<WebRtcEvent> = eventHandler.getEvents()

    override suspend fun getRoomList(): Flow<List<String>?> = signaling.getRoomList()

    override fun sendMessage(message: String) {
        webRtcController.sendMessage(message)
    }

    override fun sendInputEvent() {
        webRtcController.sendInputEvent()
    }

    override fun getMessages(): Flow<Message> = webRtcController.getMessages()

    override fun toggleVoice() {
        webRtcController.toggleVoice()
    }

    override fun toggleVideo() {
        webRtcController.toggleVideo()
    }

    override fun getLocalSurface(): SurfaceViewRenderer =
        webRtcController.getLocalSurface()

    override fun getRemoteSurface(): SurfaceViewRenderer =
        webRtcController.getRemoteSurface()

    override fun disconnect() {
        webRtcScope.launch {
            webRtcController.dispose()
            webRtcController.closeConnection()
            signaling.terminate()
            coroutineContext.cancelChildren()
        }
    }
}
