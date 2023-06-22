package com.example.domain.event

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

sealed class SignalEvent{
    data class IceCandidateReceived (val data: IceCandidate) : SignalEvent()
    data class AnswerReceived (val data: SessionDescription) : SignalEvent()
    data class OfferReceived (val data: SessionDescription) : SignalEvent()
}
