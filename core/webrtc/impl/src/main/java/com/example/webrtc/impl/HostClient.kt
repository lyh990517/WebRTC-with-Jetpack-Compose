package com.example.webrtc.impl

import com.example.manager.FireStoreManager
import com.example.manager.HostEvent
import com.example.manager.PeerConnectionManager
import com.example.manager.ResourceManager
import com.example.manager.hostEvent
import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class HostClient @Inject constructor(
    private val hostController: HostController,
    private val peerConnectionManager: PeerConnectionManager,
    private val resourceManager: ResourceManager,
    private val fireStoreManager: FireStoreManager
) : WebRtcClient {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun connect(roomID: String) {
        scope.launch {
            hostController.start()

            peerConnectionManager.connectToPeerAsHost(roomID)

            resourceManager.startCapture()

            hostEvent.emit(HostEvent.SendOffer(roomID))

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