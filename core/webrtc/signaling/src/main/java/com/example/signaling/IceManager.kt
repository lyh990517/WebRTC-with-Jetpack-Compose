package com.example.signaling

import android.util.Log
import com.example.common.WebRtcEvent
import com.example.model.CandidateType
import com.example.model.Packet
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

class IceManager @Inject constructor(
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
            val type = if (isHost) CandidateType.OFFER.value else CandidateType.ANSWER.value

            initializeIceField(type)

            getIce().collect(::handleIceCandidate)
        }
    }

    fun sendIce(
        ice: IceCandidate?,
        type: CandidateType
    ) {
        if (ice == null) return

        val parsedIceCandidate = ice.parseDate(type.value)

        Log.e("send", "ice : $parsedIceCandidate")

        getIceField(type.value)
            .update("candidates", FieldValue.arrayUnion(parsedIceCandidate))
    }

    fun getEvent() = iceEvent.asSharedFlow()

    @OptIn(FlowPreview::class)
    private fun getIce() = flow {
        callbackFlow {
            val candidate = if (isHost) CandidateType.ANSWER else CandidateType.OFFER
            val collection = getIceField(candidate.value)

            val listener = collection
                .addSnapshotListener { snapshot, e ->
                    val candidates = snapshot?.get("candidates") as? List<*>

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


    private fun initializeIceField(type: String) {
        getIceField(type)
            .set(mapOf("candidates" to listOf<Any>()))
    }

    private suspend fun handleIceCandidate(packet: Packet) {
        val iceCandidate = packet.toIceCandidate()

        iceEvent.emit(WebRtcEvent.Guest.SetRemoteIce(iceCandidate))
        iceEvent.emit(WebRtcEvent.Host.SetRemoteIce(iceCandidate))
    }

    private fun getIceField(type: String) = firestore
        .collection(ROOT)
        .document(roomID)
        .collection(ICE_CANDIDATE)
        .document(type)

    companion object {
        private const val ROOT = "calls"
        private const val ICE_CANDIDATE = "candidates"
    }
}