package com.example.domain.client

interface WebRTCClient {
    fun toggleVoice()

    fun toggleVideo()

    fun disconnect()

    fun connect(roomID: String, isJoin: Boolean)
}