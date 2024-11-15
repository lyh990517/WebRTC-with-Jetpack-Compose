package com.example.webrtc.impl

import com.example.manager.PeerConnectionManager
import com.example.manager.ResourceManager
import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class GuestClient @Inject constructor(
    private val webRtcScope: CoroutineScope,
    private val eventController: EventController,
    private val peerConnectionManager: PeerConnectionManager,
    private val resourceManager: ResourceManager,
    private val fireStoreManager: com.example.firestore.FireStoreManager
) : WebRtcClient {

    override fun connect(roomID: String) {
        webRtcScope.launch {
            eventController.start()

            peerConnectionManager.connectToPeerAsGuest(roomID)

            resourceManager.startCapture()

            fireStoreManager.observeSignaling(roomID)
        }
    }

    override fun toggleVoice() {
        resourceManager.toggleVoice()
    }

    override fun toggleVideo() {
        resourceManager.toggleVideo()
    }

    override suspend fun getRoomStatus(roomID: String): RoomStatus =
        fireStoreManager.getRoomStatus(roomID).first()

    override fun disconnect() {
        webRtcScope.cancel()
        resourceManager.dispose()
        peerConnectionManager.closeConnection()
    }
}