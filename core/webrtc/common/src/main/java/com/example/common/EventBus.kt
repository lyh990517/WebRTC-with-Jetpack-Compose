package com.example.common

import kotlinx.coroutines.flow.MutableSharedFlow

object EventBus {
    val eventFlow = MutableSharedFlow<WebRtcEvent>()
}
