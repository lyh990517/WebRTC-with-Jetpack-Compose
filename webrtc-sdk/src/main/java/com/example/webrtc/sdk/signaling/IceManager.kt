package com.example.webrtc.sdk.signaling

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.webrtc.sdk.event.WebRtcEvent
import com.example.webrtc.sdk.model.Packet
import com.example.webrtc.sdk.signaling.SignalingImpl.Companion.ROOT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate

internal class IceManager(
    private val firestore: FirebaseFirestore,
    private val webrtcScope: CoroutineScope
) {
    private val iceEvent = MutableSharedFlow<WebRtcEvent>()
    private var roomID = ""
    private var isHost = false

    fun collectIce(isHost: Boolean, roomID: String) {
        this.roomID = roomID
        this.isHost = isHost

        webrtcScope.launch {
            setFieldToIce()

            getIceFlow().collect(::handleIceCandidate)
        }
    }

    fun sendIce(
        ice: IceCandidate?,
        type: SignalType
    ) {
        if (ice == null) return

        val parsedIceCandidate = ice.parseDate(type.value)

        getIce(type.value)
            .update(ICE_FIELD, FieldValue.arrayUnion(parsedIceCandidate))
    }

    fun getEvent() = iceEvent.asSharedFlow()

    @OptIn(FlowPreview::class)
    private fun getIceFlow() =
        callbackFlow {
            val type = if (isHost) SignalType.ANSWER else SignalType.OFFER

            val listener = getIce(type.value).observeIce(::trySend)

            awaitClose { listener.remove() }
        }.debounce(300)

    private fun setFieldToIce() {
        val type = if (isHost) SignalType.OFFER.value else SignalType.ANSWER.value

        getIce(type)
            .set(mapOf(ICE_FIELD to listOf<Any>()))
    }

    private suspend fun handleIceCandidate(packets: List<Packet>) {
        packets.forEach { packet ->
            val iceCandidate = packet.toIceCandidate()

            if (isHost) {
                iceEvent.emit(WebRtcEvent.Host.SetRemoteIce(iceCandidate))
            } else {
                iceEvent.emit(WebRtcEvent.Guest.SetRemoteIce(iceCandidate))
            }
        }
    }

    private fun getIce(type: String) = firestore
        .collection(ROOT)
        .document(roomID)
        .collection(ICE_COLLECTION)
        .document(type)

    companion object {
        private const val ICE_COLLECTION = "ice"
        private const val ICE_FIELD = "ices"
    }
}
