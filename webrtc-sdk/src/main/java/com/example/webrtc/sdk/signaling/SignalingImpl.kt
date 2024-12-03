package com.example.webrtc.sdk.signaling

import com.google.firebase.firestore.FirebaseFirestore
import com.example.webrtc.sdk.event.WebRtcEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.time.LocalDateTime

internal class SignalingImpl(
    private val firestore: FirebaseFirestore,
    private val sdpManager: SdpManager,
    private val iceManager: IceManager
) : Signaling {
    private var roomID = ""

    override suspend fun getRoomExists(roomID: String): Boolean {
        val room = getRoom(roomID)
        val roomExists = room.get().await().exists()

        if (!roomExists) {
            room.set(mapOf("startedAt" to "${LocalDateTime.now()}"))
        }

        return roomExists
    }

    override suspend fun getRoomList() = callbackFlow {
        val listener = firestore.collection(ROOT).addSnapshotListener { value, error ->
            trySend(value?.documents?.map { it.id })
        }
        awaitClose { listener.remove() }
    }

    override suspend fun terminate() {
        getRoom(roomID).delete().await()
    }

    override suspend fun sendIce(
        ice: IceCandidate?,
        type: SignalType
    ) {
        iceManager.sendIce(ice, type)
    }

    override suspend fun sendSdp(
        sdp: SessionDescription
    ) {
        sdpManager.sendSdp(sdp)
    }

    override suspend fun start(roomID: String, isHost: Boolean) {
        this.roomID = roomID

        firestore.enableNetwork()

        sdpManager.collectSdp(isHost, roomID)

        iceManager.collectIce(isHost, roomID)
    }

    override fun getEvent(): Flow<WebRtcEvent> = merge(sdpManager.getEvent(), iceManager.getEvent())

    private fun getRoom(roomId: String) = firestore
        .collection(ROOT)
        .document(roomId)

    companion object {
        const val ROOT = "calls"
    }
}
