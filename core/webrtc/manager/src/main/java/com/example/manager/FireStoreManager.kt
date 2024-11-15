package com.example.manager

import android.util.Log
import com.example.model.Candidate
import com.example.model.Packet.Companion.isAnswer
import com.example.model.Packet.Companion.isOffer
import com.example.model.RoomStatus
import com.example.util.parseData
import com.example.util.parseDate
import com.example.util.toAnswerSdp
import com.example.util.toIceCandidate
import com.example.util.toOfferSdp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

class FireStoreManager @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val fireStoreScope = CoroutineScope(Dispatchers.IO)

    fun getRoomStatus(roomID: String): Flow<RoomStatus> = callbackFlow {
        getRoom(roomID)
            .get()
            .addOnSuccessListener { data ->
                fireStoreScope.launch {
                    val isRoomEnded = data["type"] == "END_CALL"
                    val roomStatus = if (isRoomEnded) RoomStatus.TERMINATED else RoomStatus.NEW

                    send(roomStatus)
                }
            }
        awaitClose {}
    }

    fun sendIceCandidateToRoom(candidate: IceCandidate?, type: Candidate, roomId: String) {
        if (candidate == null) return

        val parsedIceCandidate = candidate.parseDate(type.value)

        getRoom(roomId)
            .collection(ICE_CANDIDATE)
            .document(type.value)
            .set(parsedIceCandidate).addOnSuccessListener {
                Log.e("FireStore", "sendIceCandidate: Success")
            }.addOnFailureListener {
                Log.e("FireStore", "sendIceCandidate: Error $it")
            }
    }

    fun sendSdpToRoom(sdp: SessionDescription, roomId: String) {
        val parsedSdp = sdp.parseData()

        getRoom(roomId)
            .set(parsedSdp)
    }

    fun observeSignaling(roomID: String) {
        fireStoreScope.launch {
            getRoomUpdates(roomID).collect { packet ->
                when {
                    packet.isOffer() -> handleOffer(packet, roomID)
                    packet.isAnswer() -> handleAnswer(packet)
                    else -> handleIceCandidate(packet)
                }
            }
        }
    }

    private fun getRoomUpdates(roomID: String) = callbackFlow {
        firestore.enableNetwork().addOnFailureListener { e ->
            fireStoreScope.launch {
                sendError(e)
            }
        }

        getRoom(roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                fireStoreScope.launch {
                    sendError(e)
                }
            }

            if (snapshot != null && snapshot.exists()) {
                fireStoreScope.launch {
                    val data = snapshot.data
                    data?.let { send(com.example.model.Packet(it)) }
                }
            }
        }

        getRoom(roomID).collection(ICE_CANDIDATE)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    fireStoreScope.launch {
                        sendError(e)
                    }
                }

                if (snapshot != null && snapshot.isEmpty.not()) {
                    snapshot.forEach { dataSnapshot ->
                        fireStoreScope.launch {
                            send(com.example.model.Packet(dataSnapshot.data))
                        }
                    }
                }
            }
        awaitClose { }
    }

    private fun handleIceCandidate(packet: com.example.model.Packet) = fireStoreScope.launch {
        val iceCandidate = packet.toIceCandidate()

        guestEvent.emit(GuestEvent.SetRemoteIce(iceCandidate))
        hostEvent.emit(HostEvent.SetRemoteIce(iceCandidate))
    }

    private fun handleAnswer(packet: com.example.model.Packet) = fireStoreScope.launch {
        val sdp = packet.toAnswerSdp()

        hostEvent.emit(HostEvent.ReceiveAnswer(sdp))
    }

    private fun handleOffer(packet: com.example.model.Packet, roomID: String) =
        fireStoreScope.launch {
            val sdp = packet.toOfferSdp()

            guestEvent.emit(GuestEvent.ReceiveOffer(sdp))

            guestEvent.emit(GuestEvent.SendAnswer(roomID))
        }

    private suspend fun ProducerScope<com.example.model.Packet>.sendError(e: Exception) {
        send(com.example.model.Packet(mapOf("error" to e)))
    }

    fun close() {
        fireStoreScope.cancel()
    }

    private fun getRoom(roomId: String) = firestore
        .collection(ROOT)
        .document(roomId)

    companion object {
        private const val ROOT = "calls"
        private const val ICE_CANDIDATE = "candidates"
    }
}
