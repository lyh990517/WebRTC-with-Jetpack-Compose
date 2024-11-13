package com.example.data.repository

import android.util.Log
import com.example.domain.Candidate
import com.example.domain.Packet
import com.example.domain.parseData
import com.example.domain.parseDate
import com.example.domain.repository.FireStoreRepository
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
import java.lang.Exception
import javax.inject.Inject

class FireStoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : FireStoreRepository {
    private val fireStoreScope = CoroutineScope(Dispatchers.IO)

    override fun getRoomInfo(roomID: String): Flow<DocumentSnapshot> = callbackFlow {
        getRoom(roomID)
            .get()
            .addOnSuccessListener {
                fireStoreScope.launch {
                    send(it)
                }
            }
        awaitClose {}
    }

    override fun sendIceCandidateToRoom(candidate: IceCandidate?, isHost: Boolean, roomId: String) {
        if (candidate == null) return

        val type = if (isHost) Candidate.OFFER else Candidate.ANSWER

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

    override fun sendSdpToRoom(sdp: SessionDescription, roomId: String) {
        val parsedSdp = sdp.parseData()

        getRoom(roomId)
            .set(parsedSdp)
    }

    override fun getRoomUpdates(roomID: String) = callbackFlow {
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
                    data?.let { send(Packet(it)) }
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
                            send(Packet(dataSnapshot.data))
                        }
                    }
                }
            }
        awaitClose { }
    }

    private suspend fun ProducerScope<Packet>.sendError(e: Exception) {
        send(Packet(mapOf("error" to e)))
    }

    private fun getRoom(roomId: String) = firestore
        .collection(ROOT)
        .document(roomId)

    companion object {
        private const val ROOT = "calls"
        private const val ICE_CANDIDATE = "candidates"
    }
}
