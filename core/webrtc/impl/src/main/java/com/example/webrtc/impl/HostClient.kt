package com.example.webrtc.impl

import com.example.model.RoomStatus
import com.example.webrtc.impl.manager.FireStoreManager
import com.example.webrtc.impl.manager.PeerConnectionManager
import com.example.webrtc.impl.manager.ResourceManager
import com.example.webrtc.impl.manager.SignalingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.webrtc.VideoCapturer
import javax.inject.Inject

internal class HostClient @Inject constructor(
    private val hostController: HostController,
    private val signalingManager: SignalingManager,
    private val peerConnectionManager: PeerConnectionManager,
    private val resourceManager: ResourceManager,
    private val fireStoreManager: FireStoreManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect(roomID: String) {
        scope.launch {
            hostController.start()

            peerConnectionManager.connectToPeerAsHost(roomID)

            resourceManager.startCapture()

            hostEvent.emit(HostEvent.SendOffer(roomID))

            signalingManager.observeSignaling(roomID)
        }
    }

    fun toggleVoice() {
        resourceManager.toggleVoice()
    }

    fun toggleVideo() {
        resourceManager.toggleVideo()
    }

    suspend fun getRoomStatus(roomID: String): RoomStatus =
        fireStoreManager.getRoomStatus(roomID).first()

    fun disconnect() {
        fireStoreManager.close()
        resourceManager.dispose()
        peerConnectionManager.closeConnection()
        signalingManager.close()
    }
}