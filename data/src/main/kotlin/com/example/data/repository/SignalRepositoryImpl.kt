package com.example.data.repository

import com.example.domain.repository.SignalRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SignalRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SignalRepository {
    override fun connect(roomID: String) = callbackFlow {
        firestore.enableNetwork().addOnFailureListener { e ->
            CoroutineScope(Dispatchers.IO).launch {
                send(mapOf("error" to e))
            }
        }

        firestore.collection("calls").document(roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    send(mapOf("error" to e))
                }
            }

            if (snapshot != null && snapshot.exists()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = snapshot.data
                    data?.let { send(it) }
                }
            }
        }

        firestore.collection("calls").document(roomID).collection("candidates")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        send(mapOf("error" to e))
                    }
                }

                if (snapshot != null && snapshot.isEmpty.not()) {
                    snapshot.forEach { dataSnapshot ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val data = dataSnapshot.data
                            send(data)
                        }
                    }
                }
            }
        awaitClose {  }
    }
}