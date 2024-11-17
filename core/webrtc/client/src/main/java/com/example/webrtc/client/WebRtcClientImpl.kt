package com.example.webrtc.client

import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer
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

    override fun toggleVoice() {
        localResourceController.toggleVoice()
    }

    override fun toggleVideo() {
        localResourceController.toggleVideo()
    }

    override suspend fun getRoomStatus(roomID: String): RoomStatus =
        signaling.getRoomStatus(roomID)

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