package com.example.common

import org.webrtc.IceCandidate
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

sealed interface WebRtcEvent {
    sealed interface Host : WebRtcEvent {
        data object SendOffer : Host

        data class ReceiveAnswer(val sdp: SessionDescription) : Host

        data class SetLocalSdp(val observer: SdpObserver, val sdp: SessionDescription) : Host

        data class SendSdpToGuest(val sdp: SessionDescription) : Host

        data class SendIceToGuest(val ice: IceCandidate) : Host

        data class SetRemoteIce(val ice: IceCandidate) : Host
    }

    sealed interface Guest : WebRtcEvent {
        data class ReceiveOffer(val sdp: SessionDescription) : Guest

        data object SendAnswer : Guest

        data class SetLocalSdp(val observer: SdpObserver, val sdp: SessionDescription) : Guest

        data class SendSdpToHost(val sdp: SessionDescription) : Guest

        data class SendIceToHost(val ice: IceCandidate) : Guest

        data class SetRemoteIce(val ice: IceCandidate) : Guest
    }
}