package com.example.webrtc.client

import com.example.common.WebRtcEvent
import com.example.model.CandidateType
import com.example.model.RoomStatus
import kotlinx.coroutines.flow.Flow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface Signaling {
    suspend fun getRoomStatus(roomID: String): RoomStatus
    suspend fun sendIce(ice: IceCandidate?, type: CandidateType)
    suspend fun sendSdp(sdp: SessionDescription)
    suspend fun start(roomID: String, isHost: Boolean)
    fun getEvent(): Flow<WebRtcEvent>
}