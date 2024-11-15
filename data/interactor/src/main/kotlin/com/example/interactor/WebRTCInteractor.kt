package com.example.interactor

import kotlinx.coroutines.flow.StateFlow

interface WebRTCInteractor {
    val webRTCStateFlow: StateFlow<WebRTCState>

    val load : (initState : WebRTCState) -> Unit

    fun close()
}