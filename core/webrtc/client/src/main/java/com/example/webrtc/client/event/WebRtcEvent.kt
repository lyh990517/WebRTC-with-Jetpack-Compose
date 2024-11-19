package com.example.webrtc.client.event

import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

sealed interface WebRtcEvent {

    sealed interface StateChange : WebRtcEvent {
        data class IceConnection(val state: PeerConnection.IceConnectionState?) : StateChange
        data class IceGathering(val state: PeerConnection.IceGatheringState?) : StateChange
        data class Signaling(val state: PeerConnection.SignalingState?) : StateChange
        data class Connection(val state: PeerConnection.PeerConnectionState?) : StateChange
        data class DataChannel(val state: org.webrtc.DataChannel.State) : StateChange
        data class Buffer(val amount: Long) : StateChange
    }

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