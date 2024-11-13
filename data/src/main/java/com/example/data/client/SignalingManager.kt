package com.example.data.client

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
import org.webrtc.MediaConstraints
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalingManager @Inject constructor(
    private val fireStoreRepository: FireStoreRepository,
    private val peerConnectionManager: PeerConnectionManager,
) {
    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }
    private val signalingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val peerConnection get() =  peerConnectionManager.getPeerConnection()

    fun observeSignaling(isHost: Boolean, roomID: String) {
        signalingScope.launch {
            if (isHost) sendOfferToGuest(roomID)

            fireStoreRepository.connectToRoom(roomID).collect { packet ->
                when {
                    packet.isOffer() -> {
                        val sdp = packet.toOfferSdp()
                        val sdpObserver = createSdpObserver()
                        peerConnection.setRemoteDescription(sdpObserver, sdp)

                        sendAnswerToHost(roomID)
                    }

                    packet.isAnswer() -> {
                        val sdp = packet.toAnswerSdp()
                        val sdpObserver = createSdpObserver()

                        peerConnection.setRemoteDescription(sdpObserver, sdp)
                    }

                    else -> {
                        val iceCandidate = packet.toIceCandidate()

                        peerConnection.addIceCandidate(iceCandidate)
                    }
                }
            }
        }
    }

    private fun sendOfferToGuest(roomID: String) {
        val sdpObserver = createSdpObserver { sdp, observer ->
            peerConnection.setLocalDescription(observer, sdp)
            fireStoreRepository.sendSdpToRoom(sdp = sdp, roomId = roomID)
        }

        peerConnection.createOffer(sdpObserver, constraints)
    }

    private fun sendAnswerToHost(roomID: String) {
        val sdpObserver = createSdpObserver { sdp, observer ->
            peerConnection.setLocalDescription(observer, sdp)
            fireStoreRepository.sendSdpToRoom(sdp = sdp, roomId = roomID)
        }

        peerConnection.createAnswer(sdpObserver, constraints)
    }

    fun close() = signalingScope.cancel()

    private fun createSdpObserver(setLocalDescription: ((SessionDescription, SdpObserver) -> Unit)? = null) =
        object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                setLocalDescription?.let { function -> p0?.let { sdp -> function(sdp, this) } }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }
}
