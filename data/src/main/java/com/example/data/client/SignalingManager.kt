package com.example.data.client

import com.example.domain.Packet
import com.example.domain.Packet.Companion.isAnswer
import com.example.domain.Packet.Companion.isOffer
import com.example.domain.Packet.Companion.toAnswerSdp
import com.example.domain.Packet.Companion.toIceCandidate
import com.example.domain.Packet.Companion.toOfferSdp
import com.example.domain.repository.FireStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalingManager @Inject constructor(
    private val fireStoreRepository: FireStoreRepository,
    private val peerConnectionManager: PeerConnectionManager,
) {
    private val signalingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun observeSignaling(isHost: Boolean, roomID: String) {
        signalingScope.launch {
            if (isHost) {
                peerConnectionManager.createOffer(roomID)
            }

            fireStoreRepository.connectToRoom(roomID).collect { packet ->
                when {
                    packet.isOffer() -> handleOffer(packet, roomID)
                    packet.isAnswer() -> handleAnswer(packet)
                    else -> handleIceCandidate(packet)
                }
            }
        }
    }

    private fun handleIceCandidate(packet: Packet) {
        val iceCandidate = packet.toIceCandidate()

        peerConnectionManager.addIceCandidate(iceCandidate)
    }

    private fun handleAnswer(packet: Packet) {
        val sdp = packet.toAnswerSdp()

        peerConnectionManager.setRemoteDescription(sdp)
    }

    private fun handleOffer(packet: Packet, roomID: String) {
        val sdp = packet.toOfferSdp()

        peerConnectionManager.setRemoteDescription(sdp)

        peerConnectionManager.createAnswer(roomID)
    }

    fun close() = signalingScope.cancel()
}
