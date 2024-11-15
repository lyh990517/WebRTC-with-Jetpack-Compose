package com.example.webrtc.impl

import com.example.firestore.Signaling
import com.example.manager.Controller
import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class WebRtcClient @Inject constructor(
    private val webRtcScope: CoroutineScope,
    private val eventHandler: EventHandler,
    private val webRtcController: Controller.WebRtc,
    private val localResourceController: Controller.LocalResource,
    private val signaling: Signaling
) : WebRtcClient {
    override fun connect(roomID: String, isHost: Boolean) {
        webRtcScope.launch {
            eventHandler.start()

            signaling.start(roomID)

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
        signaling.getRoomStatus(roomID).first()

    override fun disconnect() {
        webRtcScope.cancel()
        localResourceController.dispose()
        webRtcController.closeConnection()
    }
}