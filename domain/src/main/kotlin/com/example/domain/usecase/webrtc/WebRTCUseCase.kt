package com.example.domain.usecase.webrtc

import javax.inject.Inject

data class WebRTCUseCase @Inject constructor(
    val sendIceCandidateUseCase: SendIceCandidateUseCase,
    val sendSdpUseCase: SendSdpUseCase,
    val getSignalEventUseCase: GetSignalEventUseCase
)
