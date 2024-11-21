package com.example.webrtc.client.api

import com.example.webrtc.client.model.Message
import com.example.webrtc.client.signaling.RoomStatus
import kotlinx.coroutines.flow.Flow
import org.webrtc.SurfaceViewRenderer

interface WebRtcClient {
    fun connect(roomID: String)

    suspend fun getRoomList(): Flow<List<String>?>

    suspend fun getRoomStatus(): Flow<RoomStatus>

    fun sendMessage(message: String)

    fun sendInputEvent()

    fun getMessages(): Flow<Message>

    fun disconnect()

    fun toggleVoice()

    fun toggleVideo()

    fun getLocalSurface(): SurfaceViewRenderer

    fun getRemoteSurface(): SurfaceViewRenderer
}
