package com.example.webrtc.impl

import com.example.model.Packet.Companion.isAnswer
import com.example.model.Packet.Companion.isOffer
import com.example.model.Packet.Companion.toAnswerSdp
import com.example.model.Packet.Companion.toIceCandidate
import com.example.model.Packet.Companion.toOfferSdp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalingManager @Inject constructor(
    private val fireStoreManager: FireStoreManager,
    private val peerConnectionManager: PeerConnectionManager,
) {
    private val signalingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun observeSignaling(isHost: Boolean, roomID: String) {
        signalingScope.launch {
            if (isHost) {
                peerConnectionManager.createOffer(roomID)
            }

            fireStoreManager.getRoomUpdates(roomID).collect { packet ->
                when {
                    packet.isOffer() -> handleOffer(packet, roomID)
                    packet.isAnswer() -> handleAnswer(packet)
                    else -> handleIceCandidate(packet)
                }
            }
        }
    }

    private fun handleIceCandidate(packet: com.example.model.Packet) {
        val iceCandidate = packet.toIceCandidate()

        peerConnectionManager.addIceCandidate(iceCandidate)
    }

    private fun handleAnswer(packet: com.example.model.Packet) {
        val sdp = packet.toAnswerSdp()

        peerConnectionManager.setRemoteDescription(sdp)
    }

    private fun handleOffer(packet: com.example.model.Packet, roomID: String) {
        val sdp = packet.toOfferSdp()

        peerConnectionManager.setRemoteDescription(sdp)

        peerConnectionManager.createAnswer(roomID)
    }

    fun close() = signalingScope.cancel()
}
