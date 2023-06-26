package com.example.domain.event

import com.example.domain.client.WebRTCClient

sealed class WebRTCEvent {
    data class Initialize(val webRTCClient: WebRTCClient) : WebRTCEvent()

    object CloseSession : WebRTCEvent()

}
