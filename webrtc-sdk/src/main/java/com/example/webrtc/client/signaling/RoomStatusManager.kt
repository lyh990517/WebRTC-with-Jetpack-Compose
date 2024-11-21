package com.example.webrtc.client.signaling

import com.example.webrtc.client.signaling.SignalingImpl.Companion.ROOT
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RoomStatusManager @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun sendStatus(roomId: String, isHost: Boolean, roomStatus: RoomStatus) {
        if (isHost) {
            getHostStatus(roomId).set(roomStatus.toMap())
        } else {
            getGuestStatus(roomId).set(roomStatus.toMap())
        }
    }

    fun getStatus(roomId: String, isHost: Boolean) = callbackFlow {
        val status = if (isHost) getGuestStatus(roomId) else getHostStatus(roomId)

        val listener = status.addSnapshotListener { snapshot, _ ->
            val roomStatus = snapshot?.toObject(RoomStatus::class.java)

            if (roomStatus != null) {
                trySend(roomStatus)
            }
        }

        awaitClose { listener.remove() }
    }

    private fun getHostStatus(roomId: String) =
        getRoom(roomId)
            .collection("status")
            .document("host")

    private fun getGuestStatus(roomId: String) =
        getRoom(roomId)
            .collection("status")
            .document("guest")

    private fun getRoom(roomId: String) = firestore
        .collection(ROOT)
        .document(roomId)
}

data class RoomStatus(
    val status: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMap() = mapOf(
        "status" to status,
        "lastUpdated" to lastUpdated
    )
}

