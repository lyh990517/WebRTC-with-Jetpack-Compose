package com.example.webrtc.client.signaling

data class RoomStatus(
    val status: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMap() = mapOf(
        "status" to status,
        "lastUpdated" to lastUpdated
    )
}