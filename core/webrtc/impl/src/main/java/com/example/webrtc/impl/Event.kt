package com.example.webrtc.impl

sealed interface HostEvent {
    data object SendOffer : HostEvent

    data object ReceiveAnswer : HostEvent

    data object SetLocalSdp: HostEvent
    
    data object SetLocalIce: HostEvent

    data object SendSdpToGuest : HostEvent

    data object SendIceToGuest : HostEvent

    data object SetRemoteSdp : HostEvent

    data object SetRemoteIce : HostEvent
}

sealed interface GuestEvent {
    data object ReceiveOffer : GuestEvent

    data object SendAnswer : GuestEvent

    data object SetLocalSdp: GuestEvent

    data object SetLocalIce: GuestEvent

    data object SendSdpToHost : GuestEvent

    data object SendIceToHost : GuestEvent

    data object SetRemoteSdp : GuestEvent

    data object SetRemoteIce : GuestEvent
}