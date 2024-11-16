package com.example.webrtc.client

import com.example.model.CandidateType
import com.example.model.RoomStatus
import kotlinx.coroutines.flow.Flow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface Signaling {
    suspend fun getRoomStatus(roomID: String): RoomStatus
    fun sendIce(roomId: String, ice: IceCandidate?, type: CandidateType)
    fun sendSdp(roomId: String, sdp: SessionDescription)
    fun start(roomID: String)
}