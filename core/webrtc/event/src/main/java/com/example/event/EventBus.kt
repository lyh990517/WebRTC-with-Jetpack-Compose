package com.example.event

import kotlinx.coroutines.flow.MutableSharedFlow

val eventFlow = MutableSharedFlow<WebRtcEvent>()