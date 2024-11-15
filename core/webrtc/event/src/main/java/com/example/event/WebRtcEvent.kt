package com.example.event

import org.webrtc.IceCandidate
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

sealed interface WebRtcEvent {
    sealed interface Host : WebRtcEvent {
        data class SendOffer(val roomId: String) : Host

        data class ReceiveAnswer(val sdp: SessionDescription) : Host

        data class SetLocalSdp(val observer: SdpObserver, val sdp: SessionDescription) : Host

        data class SetLocalIce(val ice: IceCandidate) : Host

        data class SendSdpToGuest(val sdp: SessionDescription, val roomId: String) : Host

        data class SendIceToGuest(val ice: IceCandidate, val roomId: String) : Host

        data class SetRemoteIce(val ice: IceCandidate) : Host
    }

    sealed interface Guest : WebRtcEvent {
        data class ReceiveOffer(val sdp: SessionDescription) : Guest

        data class SendAnswer(val roomId: String) : Guest

        data class SetLocalSdp(val observer: SdpObserver, val sdp: SessionDescription) : Guest

        data class SetLocalIce(val ice: IceCandidate) : Guest

        data class SendSdpToHost(val sdp: SessionDescription, val roomId: String) : Guest

        data class SendIceToHost(val ice: IceCandidate, val roomId: String) : Guest

        data class SetRemoteIce(val ice: IceCandidate) : Guest
    }
}