package com.example.webrtc.impl

import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import com.example.webrtc.impl.manager.FireStoreManager
import com.example.webrtc.impl.manager.PeerConnectionManager
import com.example.webrtc.impl.manager.ResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class GuestClient @Inject constructor(
    private val guestController: GuestController,
    private val peerConnectionManager: PeerConnectionManager,
    private val resourceManager: ResourceManager,
    private val fireStoreManager: FireStoreManager
) : WebRtcClient {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun connect(roomID: String) {
        scope.launch {
            guestController.start()

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
        fireStoreManager.close()
        resourceManager.dispose()
        peerConnectionManager.closeConnection()
    }
}