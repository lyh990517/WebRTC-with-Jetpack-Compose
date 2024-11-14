package com.example.webrtc.api

interface WebRtcClient {
    fun toggleVoice()

    fun toggleVideo()

    fun disconnect()

    fun connect(roomID: String, isHost: Boolean)
}
