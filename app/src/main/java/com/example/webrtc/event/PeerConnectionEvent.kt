package com.example.webrtc.event

import org.webrtc.IceCandidate
import org.webrtc.MediaStream

sealed class PeerConnectionEvent {
    data class OnAddStream(val data: MediaStream) : PeerConnectionEvent()
    data class OnIceCandidate(val data: IceCandidate) : PeerConnectionEvent()
}
