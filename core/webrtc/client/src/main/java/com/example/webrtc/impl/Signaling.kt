package com.example.webrtc.impl

import com.example.model.CandidateType
import com.example.model.RoomStatus
import kotlinx.coroutines.flow.Flow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface Signaling {
    fun getRoomStatus(roomID: String): Flow<RoomStatus>
    fun sendIce(roomId: String, ice: IceCandidate?, type: CandidateType)
    fun sendSdp(roomId: String, sdp: SessionDescription)
    fun start(roomID: String)
}