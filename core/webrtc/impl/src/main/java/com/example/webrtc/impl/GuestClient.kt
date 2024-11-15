package com.example.webrtc.impl

import com.example.manager.FireStoreManager
import com.example.manager.PeerConnectionManager
import com.example.manager.ResourceManager
import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class GuestClient @Inject constructor(
    private val eventController: EventController,
    private val peerConnectionManager: PeerConnectionManager,
    private val resourceManager: ResourceManager,
    private val fireStoreManager: FireStoreManager
) : WebRtcClient {

    override fun connect(roomID: String) {
        eventController.start()

        peerConnectionManager.connectToPeerAsGuest(roomID)

        resourceManager.startCapture()

        fireStoreManager.observeSignaling(roomID)
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
        fireStoreManager.close()
        resourceManager.dispose()
        peerConnectionManager.closeConnection()
    }
}