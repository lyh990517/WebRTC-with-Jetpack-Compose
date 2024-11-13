package com.example.domain.repository

import com.example.domain.Packet
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface FireStoreRepository {
    fun getRoomInfo(roomID: String): Flow<DocumentSnapshot>

    fun sendIceCandidateToRoom(candidate: IceCandidate?, isHost: Boolean, roomId: String)

    fun sendSdpToRoom(sdp: SessionDescription, roomId: String)

    fun getRoomUpdates(roomID: String): Flow<Packet>
}
