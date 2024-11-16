package com.example.signaling

import com.example.common.EventBus.eventFlow
import com.example.common.WebRtcEvent
import com.example.model.CandidateType
import com.example.model.Packet
import com.example.model.Packet.Companion.isAnswer
import com.example.model.Packet.Companion.isOffer
import com.example.model.RoomStatus
import com.example.util.parseData
import com.example.util.parseDate
import com.example.util.toAnswerSdp
import com.example.util.toIceCandidate
import com.example.util.toOfferSdp
import com.example.webrtc.client.Signaling
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SignalingImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : Signaling {
    override suspend fun getRoomStatus(roomID: String): RoomStatus {
        val data = getRoom(roomID)
            .get()
            .await()

        val isRoomEnded = data["type"] == "END_CALL"
        val roomStatus = if (isRoomEnded) RoomStatus.TERMINATED else RoomStatus.NEW

        return roomStatus
    }

    override suspend fun sendIce(
        roomId: String,
        ice: IceCandidate?,
        type: CandidateType
    ) {
        if (ice == null) return

        val parsedIceCandidate = ice.parseDate(type.value)

        getRoom(roomId)
            .collection(ICE_CANDIDATE)
            .document(type.value)
            .set(parsedIceCandidate)
    }

    override suspend fun sendSdp(
        roomId: String,
        sdp: SessionDescription
    ) {
        val parsedSdp = sdp.parseData()

        getRoom(roomId)
            .set(parsedSdp)
    }

    override suspend fun start(roomID: String) {
        getRoomUpdates(roomID).collect { packet ->
            when {
                packet.isOffer() -> handleOffer(packet, roomID)
                packet.isAnswer() -> handleAnswer(packet)
                else -> handleIceCandidate(packet)
            }
        }
    }

    private fun getRoomUpdates(roomID: String) = callbackFlow {
        firestore.enableNetwork().addOnFailureListener { e ->
            sendError(e)
        }

        getRoom(roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                sendError(e)
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                data?.let { trySend(Packet(it)) }
            }
        }

        getRoom(roomID).collection(ICE_CANDIDATE)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    sendError(e)
                }

                if (snapshot != null && snapshot.isEmpty.not()) {
                    snapshot.forEach { dataSnapshot ->
                        trySend(Packet(dataSnapshot.data))
                    }
                }
            }
        awaitClose { }
    }

    private suspend fun handleIceCandidate(packet: Packet) {
        val iceCandidate = packet.toIceCandidate()

        eventFlow.emit(WebRtcEvent.Guest.SetRemoteIce(iceCandidate))
        eventFlow.emit(WebRtcEvent.Host.SetRemoteIce(iceCandidate))
    }

    private suspend fun handleAnswer(packet: Packet) {
        val sdp = packet.toAnswerSdp()

        eventFlow.emit(WebRtcEvent.Host.ReceiveAnswer(sdp))
    }

    private suspend fun handleOffer(packet: Packet, roomID: String) {
        val sdp = packet.toOfferSdp()

        eventFlow.emit(WebRtcEvent.Guest.ReceiveOffer(sdp))

        eventFlow.emit(WebRtcEvent.Guest.SendAnswer(roomID))
    }

    private fun ProducerScope<Packet>.sendError(e: Exception) {
        trySend(Packet(mapOf("error" to e)))
    }

    private fun getRoom(roomId: String) = firestore
        .collection(ROOT)
        .document(roomId)

    companion object {
        private const val ROOT = "calls"
        private const val ICE_CANDIDATE = "candidates"
    }
}
