package com.example.webrtc.data

import kotlinx.coroutines.flow.Flow

interface WebRTCRepository {
    fun connect(roomID: String) : Flow<Map<String, Any>>
}