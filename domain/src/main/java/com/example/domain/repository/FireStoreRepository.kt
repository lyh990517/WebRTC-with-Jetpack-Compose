package com.example.domain.repository

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface FireStoreRepository {
    fun getRoomInfo(roomID: String): Flow<DocumentSnapshot>
}