package com.example.domain.client

interface WebRtcClient {
    fun toggleVoice()

    fun toggleVideo()

    fun disconnect()

    fun connect(roomID: String, isHost: Boolean)
}
