package com.example.webrtc.sdk.api

import com.example.webrtc.sdk.event.WebRtcEvent
import com.example.webrtc.sdk.model.Message
import kotlinx.coroutines.flow.Flow
import org.webrtc.SurfaceViewRenderer

interface WebRtcClient {
    fun connect(roomID: String)

    fun startCapture()

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
