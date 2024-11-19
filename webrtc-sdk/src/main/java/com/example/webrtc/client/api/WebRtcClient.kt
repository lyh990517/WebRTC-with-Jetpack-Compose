package com.example.webrtc.client.api

import com.example.webrtc.client.model.Message
import kotlinx.coroutines.flow.Flow
import org.webrtc.SurfaceViewRenderer

interface WebRtcClient {
    fun connect(roomID: String)

    fun sendMessage(message: Message)

    fun getMessages(): Flow<Message>

    fun disconnect()

    fun toggleVoice()

    fun toggleVideo()

    fun getLocalSurface(): SurfaceViewRenderer

    fun getRemoteSurface(): SurfaceViewRenderer
}
