package com.example.domain.repository

import com.google.firebase.firestore.DocumentReference

interface SignalRepository {
    val connect : (roomId : String) -> DocumentReference
}