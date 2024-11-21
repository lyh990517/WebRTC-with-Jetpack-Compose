package com.example.webrtc.client.signaling

import com.example.webrtc.client.signaling.SignalingImpl.Companion.ROOT
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PeerStatusManager @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun sendMyStatus(roomId: String, isHost: Boolean, peerStatus: PeerStatus) {
        if (isHost) {
            getHostStatus(roomId).set(peerStatus.toMap())
        } else {
            getGuestStatus(roomId).set(peerStatus.toMap())
        }
    }

    fun getPeerStatus(roomId: String, isHost: Boolean) = callbackFlow {
        val status = if (isHost) getGuestStatus(roomId) else getHostStatus(roomId)

        val listener = status.addSnapshotListener { snapshot, _ ->
            val peerStatus = snapshot?.toObject(PeerStatus::class.java)

            if (peerStatus != null) {
                trySend(peerStatus)
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

