package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface SignalRepository {
    fun connect(roomID: String) : Flow<Map<String, Any>>
}