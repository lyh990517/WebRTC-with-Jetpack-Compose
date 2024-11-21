package com.example.webrtc.client

import android.graphics.Bitmap
import com.example.webrtc.client.api.WebRtcClient
import com.example.webrtc.client.controller.Controller
import com.example.webrtc.client.signaling.Signaling
import com.example.webrtc.client.event.EventHandler
import com.example.webrtc.client.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WebRtcClientImpl @Inject constructor(
    private val webRtcScope: CoroutineScope,
    private val eventHandler: EventHandler,
    private val webRtcController: Controller.WebRtc,
    private val localResourceController: Controller.LocalResource,
    private val signaling: Signaling
) : WebRtcClient {
    override fun connect(roomID: String) {
        webRtcScope.launch {
            val isHost = !signaling.getRoomExists(roomID)

            launch { eventHandler.start() }

            launch { signaling.start(roomID, isHost) }

            webRtcController.connect(roomID, isHost)

            localResourceController.startCapture()
        }
    }

    override suspend fun getRoomList(): Flow<List<String>?> = signaling.getRoomList()

    override fun sendMessage(message: String) {
        webRtcController.sendMessage(message)
    }

    override fun sendImage(bitmap: Bitmap) {
        webRtcController.sendImage(bitmap)
    }

    override fun sendFile(file: File) {
        webRtcController.sendFile(file)
    }

    override fun getMessages(): Flow<Message> = webRtcController.getMessages()

    override fun toggleVoice() {
        localResourceController.toggleVoice()
    }

    override fun toggleVideo() {
        localResourceController.toggleVideo()
    }

    override fun getLocalSurface(): SurfaceViewRenderer =
        localResourceController.getLocalSurface()

    override fun getRemoteSurface(): SurfaceViewRenderer =
        localResourceController.getRemoteSurface()

    override fun disconnect() {
        webRtcScope.launch {
            localResourceController.dispose()
            webRtcController.closeConnection()
            signaling.terminate()
            coroutineContext.cancelChildren()
        }
    }
}