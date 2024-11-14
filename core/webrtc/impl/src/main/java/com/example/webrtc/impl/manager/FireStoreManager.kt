package com.example.webrtc.impl.manager

import android.util.Log
import com.example.util.parseData
import com.example.util.parseDate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

internal class FireStoreManager @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val fireStoreScope = CoroutineScope(Dispatchers.IO)

    fun getRoomInfo(roomID: String): Flow<DocumentSnapshot> = callbackFlow {
        getRoom(roomID)
            .get()
            .addOnSuccessListener {
                fireStoreScope.launch {
                    send(it)
                }
            }
        awaitClose {}
    }

    fun sendIceCandidateToRoom(candidate: IceCandidate?, isHost: Boolean, roomId: String) {
        if (candidate == null) return

        val type = if (isHost) com.example.model.Candidate.OFFER else com.example.model.Candidate.ANSWER

        val parsedIceCandidate = candidate.parseDate(type.value)

        getRoom(roomId)
            .collection(ICE_CANDIDATE)
            .document(type.value)
            .set(parsedIceCandidate).addOnSuccessListener {
                Log.e("FireStore", "sendIceCandidate: Success")
            }.addOnFailureListener {
                Log.e("FireStore", "sendIceCandidate: Error $it")
            }
    }

    fun sendSdpToRoom(sdp: SessionDescription, roomId: String) {
        val parsedSdp = sdp.parseData()

        getRoom(roomId)
            .set(parsedSdp)
    }

    fun getRoomUpdates(roomID: String) = callbackFlow {
        firestore.enableNetwork().addOnFailureListener { e ->
            fireStoreScope.launch {
                sendError(e)
            }
        }

        getRoom(roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                fireStoreScope.launch {
                    sendError(e)
                }
            }

            if (snapshot != null && snapshot.exists()) {
                fireStoreScope.launch {
                    val data = snapshot.data
                    data?.let { send(com.example.model.Packet(it)) }
                }
            }
        }

        getRoom(roomID).collection(ICE_CANDIDATE)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    fireStoreScope.launch {
                        sendError(e)
                    }
                }

                if (snapshot != null && snapshot.isEmpty.not()) {
                    snapshot.forEach { dataSnapshot ->
                        fireStoreScope.launch {
                            send(com.example.model.Packet(dataSnapshot.data))
                        }
                    }
                }
            }
        awaitClose { }
    }

    private suspend fun ProducerScope<com.example.model.Packet>.sendError(e: Exception) {
        send(com.example.model.Packet(mapOf("error" to e)))
    }

    private fun getRoom(roomId: String) = firestore
        .collection(ROOT)
        .document(roomId)

    companion object {
        private const val ROOT = "calls"
        private const val ICE_CANDIDATE = "candidates"
    }
}
