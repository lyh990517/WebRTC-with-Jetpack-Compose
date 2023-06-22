package com.example.webrtc.event

import com.example.webrtc.client.WebRTCClient

sealed class WebRTCEvent {
    data class Initialize(val webRTCClient: WebRTCClient) : WebRTCEvent()
}
