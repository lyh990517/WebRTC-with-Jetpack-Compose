package com.example.webrtc.impl

import com.example.event.WebRtcEvent
import com.example.event.eventFlow
import com.example.manager.FireStoreManager
import com.example.manager.PeerConnectionManager
import com.example.manager.ResourceManager
import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class HostClient @Inject constructor(
    private val webRtcScope: CoroutineScope,
    private val eventController: EventController,
    private val peerConnectionManager: PeerConnectionManager,
    private val resourceManager: ResourceManager,
    private val fireStoreManager: FireStoreManager
) : WebRtcClient {
    override fun connect(roomID: String) {
        webRtcScope.launch {
            eventController.start()

            peerConnectionManager.connectToPeerAsHost(roomID)

            resourceManager.startCapture()

            eventFlow.emit(WebRtcEvent.Host.SendOffer(roomID))

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