package com.example.webrtc.impl

import com.example.model.Candidate
import com.example.model.RoomStatus
import kotlinx.coroutines.flow.Flow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface Signaling {
    fun getRoomStatus(roomID: String): Flow<RoomStatus>
    fun sendIceCandidateToRoom(candidate: IceCandidate?, type: Candidate, roomId: String)
    fun sendSdpToRoom(sdp: SessionDescription, roomId: String)
    fun start(roomID: String)
}