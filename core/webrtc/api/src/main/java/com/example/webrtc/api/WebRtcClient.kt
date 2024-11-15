package com.example.webrtc.api

import com.example.model.RoomStatus
import org.webrtc.SurfaceViewRenderer

interface WebRtcClient {
    fun connect(roomID: String, isHost: Boolean)

    fun disconnect()

    fun toggleVoice()

    fun toggleVideo()

    suspend fun getRoomStatus(roomID: String): RoomStatus

    fun getLocalSurface(): SurfaceViewRenderer

    fun getRemoteSurface(): SurfaceViewRenderer
}
