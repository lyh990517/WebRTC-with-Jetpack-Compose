package com.example.webrtc.impl

import kotlinx.coroutines.flow.MutableSharedFlow
import org.webrtc.IceCandidate
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

val hostEvent = MutableSharedFlow<HostEvent>()
val guestEvent = MutableSharedFlow<GuestEvent>()

sealed interface HostEvent {
    data class SendOffer(val roomId: String) : HostEvent

    data class ReceiveAnswer(val sdp: SessionDescription) : HostEvent

    data class SetLocalSdp(val observer: SdpObserver, val sdp: SessionDescription) : HostEvent

    data class SetLocalIce(val ice: IceCandidate) : HostEvent

    data class SendSdpToGuest(val sdp: SessionDescription, val roomId: String) : HostEvent

    data class SendIceToGuest(val ice: IceCandidate, val roomId: String) : HostEvent

    data class SetRemoteIce(val ice: IceCandidate) : HostEvent
}

sealed interface GuestEvent {
    data class ReceiveOffer(val sdp: SessionDescription) : GuestEvent

    data class SendAnswer(val roomId: String) : GuestEvent

    data class SetLocalSdp(val observer: SdpObserver, val sdp: SessionDescription) : GuestEvent

    data class SetLocalIce(val ice: IceCandidate) : GuestEvent

    data class SendSdpToHost(val sdp: SessionDescription, val roomId: String) : GuestEvent

    data class SendIceToHost(val ice: IceCandidate, val roomId: String) : GuestEvent

    data class SetRemoteIce(val ice: IceCandidate) : GuestEvent
}