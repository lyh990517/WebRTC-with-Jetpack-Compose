package com.example.signaling

import com.example.common.WebRtcEvent
import com.example.model.Packet
import com.example.model.Packet.Companion.isAnswer
import com.example.model.Packet.Companion.isOffer
import com.example.model.SignalType
import com.example.signaling.SignalingImpl.Companion.ROOT
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SdpManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val webrtcScope: CoroutineScope
) {
    private val sdpEvent = MutableSharedFlow<WebRtcEvent>()
    private var isHost = false
    private var roomID = ""

    fun collectSdp(isHost: Boolean, roomID: String) {
        this.isHost = isHost
        this.roomID = roomID

        webrtcScope.launch {
            getSdp().collect(::handleSdp)
        }
    }

    fun sendSdp(
        sdp: SessionDescription
    ) {
        val parsedSdp = sdp.parseData()

        getSdp(sdp.type.name)
            .set(parsedSdp)
    }

    fun getEvent() = sdpEvent.asSharedFlow()

    private fun getSdp(): Flow<Packet> {
        val type = if (isHost) SignalType.ANSWER else SignalType.OFFER

        return getSdp(type)
    }

    private fun getSdp(signalType: SignalType) = callbackFlow {
        val listener = getSdp(signalType.value)
            .addSnapshotListener { snapshot, _ ->

                val data = snapshot?.data

                if (data != null) {
                    val packet = Packet(data)

                    trySend(packet)
                }
            }

        awaitClose { listener.remove() }
    }

    private suspend fun handleSdp(packet: Packet) {
        when {
            packet.isOffer() -> {
                val sdp = packet.toOfferSdp()

                sdpEvent.emit(WebRtcEvent.Guest.ReceiveOffer(sdp))
                sdpEvent.emit(WebRtcEvent.Guest.SendAnswer)
            }

            packet.isAnswer() -> {
                val sdp = packet.toAnswerSdp()

                sdpEvent.emit(WebRtcEvent.Host.ReceiveAnswer(sdp))
            }
        }
    }

    private fun getSdp(type: String) = firestore
        .collection(ROOT)
        .document(roomID)
        .collection(SDP)
        .document(type)

    companion object {
        private const val SDP = "sdp"
    }
}