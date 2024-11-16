package com.example.signaling

import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SignalingImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val webrtcScope: CoroutineScope
) : Signaling {
    private val signalingEvent = MutableSharedFlow<WebRtcEvent>()

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
            .collection(SDP)
            .document(sdp.type.name)
            .set(parsedSdp)
    }

    override suspend fun start(roomID: String, isHost: Boolean) {
        firestore.enableNetwork()

        // TODO answer sdp 무한정 보내는 버그 수정
        webrtcScope.launch {
            getSdpUpdate(roomID, isHost).collect { packet ->
                when {
                    packet.isOffer() -> handleOffer(packet, roomID)
                    packet.isAnswer() -> handleAnswer(packet)
                }
            }
        }

        webrtcScope.launch {
            getIceUpdate(roomID).collect { packet ->
                handleIceCandidate(packet)
            }
        }
    }

    override fun getEvent(): SharedFlow<WebRtcEvent> = signalingEvent.asSharedFlow()

    private fun getSdpUpdate(roomID: String, isHost: Boolean) = callbackFlow {
        getRoom(roomID).collection(SDP).addSnapshotListener { snapshot, e ->
            if (e != null) {
                sendError(e)
            }

            if (snapshot != null && snapshot.isEmpty.not()) {
                snapshot.forEach { dataSnapshot ->
                    val packet = Packet(dataSnapshot.data)

                    when {
                        isHost && packet.isAnswer() -> {
                            trySend(packet)
                        }

                        !isHost && packet.isOffer() -> {
                            trySend(packet)
                        }
                    }
                }
            }
        }
        awaitClose { }
    }

    private fun getIceUpdate(roomID: String) = callbackFlow {
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

        signalingEvent.emit(WebRtcEvent.Guest.SetRemoteIce(iceCandidate))
        signalingEvent.emit(WebRtcEvent.Host.SetRemoteIce(iceCandidate))
    }

    private suspend fun handleAnswer(packet: Packet) {
        val sdp = packet.toAnswerSdp()

        signalingEvent.emit(WebRtcEvent.Host.ReceiveAnswer(sdp))
    }

    private suspend fun handleOffer(packet: Packet, roomID: String) {
        val sdp = packet.toOfferSdp()

        signalingEvent.emit(WebRtcEvent.Guest.ReceiveOffer(sdp))

        signalingEvent.emit(WebRtcEvent.Guest.SendAnswer(roomID))
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
        private const val SDP = "sdp"
    }
}
