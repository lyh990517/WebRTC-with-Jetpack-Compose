package com.example.data.repository

import android.util.Log
import com.example.domain.Packet
import com.example.domain.repository.FireStoreRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

class FireStoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FireStoreRepository {

    override fun getRoomInfo(roomID: String): Flow<DocumentSnapshot> = callbackFlow {
        firestore.collection("calls")
            .document(roomID)
            .get()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    send(it)
                }
            }
        awaitClose {}
    }

    override fun sendIceCandidateToRoom(candidate: IceCandidate?, isHost: Boolean, roomId: String) {
        val type = if (isHost) "offerCandidate" else "answerCandidate"

        val iceCandidate = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp,
            "type" to type
        )

        firestore.collection("calls").document(roomId).collection("candidates").document(type)
            .set(iceCandidate).addOnSuccessListener {
                Log.e("FireStore", "sendIceCandidate: Success")
            }.addOnFailureListener {
                Log.e("FireStore", "sendIceCandidate: Error $it")
            }
    }

    override fun sendSdpToRoom(sdp: SessionDescription, roomId: String) {
        firestore.collection("calls").document(roomId).set(
            hashMapOf<String, Any>(
                "sdp" to sdp.description,
                "type" to sdp.type
            )
        )
    }

    override fun connectToRoom(roomID: String) = callbackFlow {
        firestore.enableNetwork().addOnFailureListener { e ->
            CoroutineScope(Dispatchers.IO).launch {
                send(Packet(mapOf("error" to e)))
            }
        }

        firestore.collection("calls").document(roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    send(Packet(mapOf("error" to e)))
                }
            }

            if (snapshot != null && snapshot.exists()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = snapshot.data
                    data?.let { send(Packet(it)) }
                }
            }
        }

        firestore.collection("calls").document(roomID).collection("candidates")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        send(Packet(mapOf("error" to e)))
                    }
                }

                if (snapshot != null && snapshot.isEmpty.not()) {
                    snapshot.forEach { dataSnapshot ->
                        CoroutineScope(Dispatchers.IO).launch {
                            send(Packet(dataSnapshot.data))
                        }
                    }
                }
            }
        awaitClose { }
    }
}