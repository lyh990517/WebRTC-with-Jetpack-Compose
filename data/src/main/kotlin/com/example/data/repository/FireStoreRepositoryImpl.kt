package com.example.data.repository

import android.content.Intent
import com.example.domain.repository.FireStoreRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
}