package com.example.signaling

import com.example.common.WebRtcEvent
import com.example.model.Packet
import com.example.model.Packet.Companion.isAnswer
import com.example.model.Packet.Companion.isOffer
import com.example.model.SignalType
import com.example.signaling.SignalingImpl.Companion.ROOT
import com.example.util.parseData
import com.example.util.toAnswerSdp
import com.example.util.toOfferSdp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.webrtc.SessionDescription
import javax.inject.Inject

class SdpManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val webrtcScope: CoroutineScope
) {
    private val sdpEvent = MutableSharedFlow<WebRtcEvent>()
    private var isHost = false
    private var roomID = ""

    fun processSdpExchange(isHost: Boolean, roomID: String) {
        this.isHost = isHost
        this.roomID = roomID

        webrtcScope.launch {
            val packet = getSdp()

            when {
                packet.isOffer() -> handleOfferSdp(packet)
                packet.isAnswer() -> handleAnswerSdp(packet)
            }
        }
    }

    fun sendSdp(
        sdp: SessionDescription
    ) {
        val parsedSdp = sdp.parseData()

        getSdpField(sdp.type.name)
            .set(parsedSdp)
    }

    fun getEvent() = sdpEvent.asSharedFlow()

    private suspend fun getSdp(): Packet {
        val packet = if (isHost) {
            getAnswerSdp().first()
        } else {
            getOfferSdp()
        }

        return packet
    }

    private suspend fun getOfferSdp(): Packet {
        val snapshot = getSdpField(SignalType.OFFER.value).get().await()

        val data = snapshot.data ?: throw Exception("there is no offer")

        val packet = Packet(data)

        return packet
    }

    private fun getAnswerSdp() = callbackFlow {
        val listener = getSdpField(SignalType.ANSWER.value)
            .addSnapshotListener { snapshot, _ ->

                val data = snapshot?.data

                if (data != null) {
                    val packet = Packet(data)

                    trySend(packet)
                }
            }

        awaitClose { listener.remove() }
    }

    private suspend fun handleAnswerSdp(packet: Packet) {
        val sdp = packet.toAnswerSdp()

        sdpEvent.emit(WebRtcEvent.Host.ReceiveAnswer(sdp))
    }

    private suspend fun handleOfferSdp(packet: Packet) {
        val sdp = packet.toOfferSdp()

        sdpEvent.emit(WebRtcEvent.Guest.ReceiveOffer(sdp))

        sdpEvent.emit(WebRtcEvent.Guest.SendAnswer)
    }

    private fun getSdpField(type: String) = firestore
        .collection(ROOT)
        .document(roomID)
        .collection(SDP)
        .document(type)

    companion object {
        private const val SDP = "sdp"
    }
}