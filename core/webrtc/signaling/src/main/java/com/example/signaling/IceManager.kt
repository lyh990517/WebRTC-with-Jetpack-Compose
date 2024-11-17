package com.example.signaling

import com.example.common.WebRtcEvent
import com.example.model.Packet
import com.example.model.SignalType
import com.example.signaling.SignalingImpl.Companion.ROOT
import com.example.util.parseDate
import com.example.util.toIceCandidate
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class IceManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val webrtcScope: CoroutineScope
) {
    private val iceEvent = MutableSharedFlow<WebRtcEvent>()
    private var roomID = ""
    private var isHost = false

    fun processIceExchange(isHost: Boolean, roomID: String) {
        this.roomID = roomID
        this.isHost = isHost

        webrtcScope.launch {
            initializeIceField()

            getIces().collect(::handleIceCandidate)
        }
    }

    fun sendIce(
        ice: IceCandidate?,
        type: SignalType
    ) {
        if (ice == null) return

        val parsedIceCandidate = ice.parseDate(type.value)

        getIceField(type.value)
            .update(ICE_FIELD, FieldValue.arrayUnion(parsedIceCandidate))
    }

    fun getEvent() = iceEvent.asSharedFlow()

    @OptIn(FlowPreview::class)
    private fun getIces() = flow {
        callbackFlow {
            val candidate = if (isHost) SignalType.ANSWER else SignalType.OFFER

            val listener = getIceField(candidate.value)
                .addSnapshotListener { snapshot, e ->
                    val candidates = snapshot?.get(ICE_FIELD) as? List<*>

                    this.trySend(candidates)
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


    private fun initializeIceField() {
        val type = if (isHost) SignalType.OFFER.value else SignalType.ANSWER.value

        getIceField(type)
            .set(mapOf(ICE_FIELD to listOf<Any>()))
    }

    private suspend fun handleIceCandidate(packet: Packet) {
        val iceCandidate = packet.toIceCandidate()

        if (isHost) {
            iceEvent.emit(WebRtcEvent.Host.SetRemoteIce(iceCandidate))
        } else {
            iceEvent.emit(WebRtcEvent.Guest.SetRemoteIce(iceCandidate))
        }
    }

    private fun getIceField(type: String) = firestore
        .collection(ROOT)
        .document(roomID)
        .collection(ICE_COLLECTION)
        .document(type)

    companion object {
        private const val ICE_COLLECTION = "ice"
        private const val ICE_FIELD = "ices"
    }
}