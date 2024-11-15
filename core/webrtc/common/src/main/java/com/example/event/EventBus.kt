package com.example.event

import kotlinx.coroutines.flow.MutableSharedFlow

object EventBus {
    val eventFlow = MutableSharedFlow<WebRtcEvent>()
}
