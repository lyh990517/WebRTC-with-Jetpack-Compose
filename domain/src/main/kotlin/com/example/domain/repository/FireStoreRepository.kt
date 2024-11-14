package com.example.domain.repository

import com.google.firebase.firestore.DocumentReference

interface FireStoreRepository {
    val roomInfo : (roomId : String) -> DocumentReference
}