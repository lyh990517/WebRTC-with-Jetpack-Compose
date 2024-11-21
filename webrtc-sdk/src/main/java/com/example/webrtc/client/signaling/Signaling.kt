package com.example.webrtc.client.signaling

import com.example.webrtc.client.event.WebRtcEvent
import kotlinx.coroutines.flow.Flow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface Signaling {
    suspend fun getRoomExists(roomID: String): Boolean
    suspend fun getRoomList(): Flow<List<String>?>
    suspend fun terminate()
    fun sendStatus(roomStatus: RoomStatus)
    suspend fun getRoomStatus(): Flow<RoomStatus>
    suspend fun sendIce(ice: IceCandidate?, type: SignalType)
    suspend fun sendSdp(sdp: SessionDescription)
    suspend fun start(roomID: String, isHost: Boolean)
    fun getEvent(): Flow<WebRtcEvent>
}