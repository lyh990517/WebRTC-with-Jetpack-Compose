package com.example.webrtc.client.api

import android.graphics.Bitmap
import com.example.webrtc.client.model.Message
import kotlinx.coroutines.flow.Flow
import org.webrtc.SurfaceViewRenderer
import java.io.File

interface WebRtcClient {
    fun connect(roomID: String)

    suspend fun getRoomList() : Flow<List<String>?>

    fun sendMessage(message: String)

    fun sendImage(bitmap: Bitmap)

    fun sendFile(file: File)

    fun getMessages(): Flow<Message>

    fun disconnect()

    fun toggleVoice()

    fun toggleVideo()

    fun getLocalSurface(): SurfaceViewRenderer

    fun getRemoteSurface(): SurfaceViewRenderer
}
