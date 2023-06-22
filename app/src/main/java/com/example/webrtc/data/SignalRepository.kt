package com.example.webrtc.data

import kotlinx.coroutines.flow.Flow

interface SignalRepository {
    fun connect(roomID: String) : Flow<Map<String, Any>>
}