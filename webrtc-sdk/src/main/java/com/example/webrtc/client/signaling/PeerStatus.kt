package com.example.webrtc.client.signaling

data class PeerStatus(
    val iceConnection: String = "",
    val signaling: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMap() = mapOf(
        "signaling" to signaling,
        "iceConnection" to iceConnection,
        "lastUpdated" to lastUpdated
    )
}