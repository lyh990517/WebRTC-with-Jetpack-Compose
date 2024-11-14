package com.example.webrtc.api

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface WebRtcClient {
    fun getRoomInformation(roomID: String): Flow<DocumentSnapshot>

    fun toggleVoice()

    fun toggleVideo()

    fun disconnect()

    fun connect(roomID: String, isHost: Boolean)
}
