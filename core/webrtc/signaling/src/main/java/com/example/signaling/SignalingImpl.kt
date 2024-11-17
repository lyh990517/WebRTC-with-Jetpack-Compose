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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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

        Log.e("send", "ice : $parsedIceCandidate")

        getRoom(roomId)
            .collection(ICE_CANDIDATE)
            .document(type.value)
            .update("candidates", FieldValue.arrayUnion(parsedIceCandidate))
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

        processSdpExchange(isHost, roomID)

        processIceExchange(isHost, roomID)
    }

    private fun processIceExchange(isHost: Boolean, roomID: String) {
        webrtcScope.launch {
            val type = if (isHost) CandidateType.OFFER.value else CandidateType.ANSWER.value

            initializeIceCandidates(roomID, type)

            getIce(roomID, isHost).collect { packet ->
                handleIceCandidate(packet)
            }
        }
    }

    private fun initializeIceCandidates(roomID: String, type: String) {
        getRoom(roomID)
            .collection(ICE_CANDIDATE)
            .document(type)
            .set(mapOf("candidates" to listOf<Any>()))
    }

    private fun processSdpExchange(isHost: Boolean, roomID: String) {
        webrtcScope.launch {
            val packet = getSdp(roomID, isHost)

            when {
                packet.isOffer() -> handleOffer(packet, roomID)
                packet.isAnswer() -> handleAnswer(packet)
            }
        }
    }

    override fun getEvent(): SharedFlow<WebRtcEvent> = signalingEvent.asSharedFlow()

    private suspend fun getSdp(roomID: String, isHost: Boolean): Packet {
        val packet = if (isHost) {
            getAnswerSdp(roomID).first()
        } else {
            getOfferSdp(roomID)
        }

        return packet
    }

    private suspend fun getOfferSdp(roomID: String): Packet {
        val collection = getRoom(roomID).collection(SDP)

        val snapshot = collection
            .document("OFFER")
            .get()
            .await()

        val data = snapshot.data ?: throw Exception("there is no offer")

        val packet = Packet(data)

        return packet
    }

    private fun getAnswerSdp(roomID: String) = callbackFlow {
        val collection = getRoom(roomID).collection(SDP)

        val listener = collection
            .document("ANSWER")
            .addSnapshotListener { snapshot, _ ->

                val data = snapshot?.data

                if (data != null) {
                    val packet = Packet(data)

                    trySend(packet)
                }
            }
        awaitClose { listener.remove() }
    }

    @OptIn(FlowPreview::class)
    private fun getIce(roomID: String, isHost: Boolean) = flow {
        callbackFlow {
            val collection = getRoom(roomID).collection(ICE_CANDIDATE)
            val candidate = if (isHost) CandidateType.ANSWER else CandidateType.OFFER

            val listener = collection
                .document(candidate.value)
                .addSnapshotListener { snapshot, e ->
                    val candidates = snapshot?.get("candidates") as? List<*>

                    trySend(candidates)
                }
            awaitClose { listener.remove() }
        }.debounce(300)
            .collect { candidates ->
                candidates?.forEach { data ->
                    val packet = Packet(data as Map<String, Any>)
                    emit(packet)
                }
            }
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

    private fun getRoom(roomId: String) = firestore
        .collection(ROOT)
        .document(roomId)

    companion object {
        private const val ROOT = "calls"
        private const val ICE_CANDIDATE = "candidates"
        private const val SDP = "sdp"
    }
}
