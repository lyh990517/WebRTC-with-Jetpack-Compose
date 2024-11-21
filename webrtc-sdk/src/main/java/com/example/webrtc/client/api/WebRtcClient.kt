package com.example.webrtc.client.api

import com.example.webrtc.client.event.WebRtcEvent
import com.example.webrtc.client.model.Message
import kotlinx.coroutines.flow.Flow
import org.webrtc.SurfaceViewRenderer

interface WebRtcClient {
    fun connect(roomID: String)

    fun getEvent(): Flow<WebRtcEvent>

    suspend fun getRoomList(): Flow<List<String>?>

    fun sendMessage(message: String)

    fun sendInputEvent()

    fun getMessages(): Flow<Message>

    fun disconnect()

    fun toggleVoice()

    fun toggleVideo()

    fun getLocalSurface(): SurfaceViewRenderer

    fun getRemoteSurface(): SurfaceViewRenderer
}
