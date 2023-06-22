package com.example.webrtc.data

import org.webrtc.IceCandidate

interface WebRTCRepository {
    fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean, roomId: String)
}