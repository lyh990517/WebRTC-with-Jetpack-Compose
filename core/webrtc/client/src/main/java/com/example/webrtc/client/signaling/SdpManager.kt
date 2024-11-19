package com.example.webrtc.client.signaling

import com.example.webrtc.client.event.WebRtcEvent
import com.example.webrtc.client.model.Packet
import com.example.webrtc.client.model.Packet.Companion.isAnswer
import com.example.webrtc.client.model.Packet.Companion.isOffer
import com.example.webrtc.client.signaling.SignalingImpl.Companion.ROOT
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
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
        val signalType = if (isHost) SignalType.ANSWER else SignalType.OFFER

        webrtcScope.launch {
            getSdpFlow(signalType).collect(::handleSdp)
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

    private fun getSdpFlow(signalType: SignalType) = callbackFlow {
        val listener = getSdp(signalType.value).observeSdp(::trySend)

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