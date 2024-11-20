package com.example.webrtc.client.api

import com.example.webrtc.client.model.Message
import kotlinx.coroutines.flow.Flow
import org.webrtc.SurfaceViewRenderer

interface WebRtcClient {
    fun connect(roomID: String)

    suspend fun getRoomList() : List<String>

    fun sendMessage(message: String)

    fun sendFile(bytes: ByteArray)

    fun getMessages(): Flow<Message>

    fun disconnect()

    fun toggleVoice()

    fun toggleVideo()

    fun getLocalSurface(): SurfaceViewRenderer

    fun getRemoteSurface(): SurfaceViewRenderer
}
