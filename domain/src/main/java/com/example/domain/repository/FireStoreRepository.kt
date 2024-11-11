package com.example.domain.repository

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface FireStoreRepository {
    fun getRoomInfo(roomID: String): Flow<DocumentSnapshot>

    fun sendIceCandidateToRoom(candidate: IceCandidate?, isJoin: Boolean, roomId: String)

    fun sendSdpToRoom(sdp: SessionDescription, roomId: String)

    fun connect(roomID: String): Flow<Map<String, Any>>
}