package com.example.webrtc.impl.manager

import com.example.model.Packet.Companion.isAnswer
import com.example.model.Packet.Companion.isOffer
import com.example.util.toAnswerSdp
import com.example.util.toIceCandidate
import com.example.util.toOfferSdp
import com.example.webrtc.impl.GuestEvent
import com.example.webrtc.impl.HostEvent
import com.example.webrtc.impl.guestEvent
import com.example.webrtc.impl.hostEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SignalingManager @Inject constructor(
    private val fireStoreManager: FireStoreManager,
) {
    private val signalingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun observeSignaling(roomID: String) {
        signalingScope.launch {
            fireStoreManager.getRoomUpdates(roomID).collect { packet ->
                when {
                    packet.isOffer() -> handleOffer(packet, roomID)
                    packet.isAnswer() -> handleAnswer(packet)
                    else -> handleIceCandidate(packet)
                }
            }
        }
    }

    private fun handleIceCandidate(packet: com.example.model.Packet) = signalingScope.launch {
        val iceCandidate = packet.toIceCandidate()

        guestEvent.emit(GuestEvent.SetRemoteIce(iceCandidate))
        hostEvent.emit(HostEvent.SetRemoteIce(iceCandidate))
    }

    private fun handleAnswer(packet: com.example.model.Packet) = signalingScope.launch {
        val sdp = packet.toAnswerSdp()

        hostEvent.emit(HostEvent.ReceiveAnswer(sdp))
    }

    private fun handleOffer(packet: com.example.model.Packet, roomID: String) =
        signalingScope.launch {
            val sdp = packet.toOfferSdp()

            guestEvent.emit(GuestEvent.ReceiveOffer(sdp))

            guestEvent.emit(GuestEvent.SendAnswer(roomID))
        }

    fun close() = signalingScope.cancel()
}
