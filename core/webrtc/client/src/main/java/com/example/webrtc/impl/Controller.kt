package com.example.webrtc.impl

import org.webrtc.IceCandidate
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

sealed interface Controller {
    interface LocalResource {
        fun toggleVoice()
        fun toggleVideo()
        fun startCapture()
        fun dispose()
    }

    interface WebRtc {
        fun connect(roomID: String, isHost: Boolean)
        fun createOffer(roomID: String)
        fun createAnswer(roomID: String)
        fun closeConnection()
        fun setLocalDescription(sdp: SessionDescription, observer: SdpObserver)
        fun setRemoteDescription(sdp: SessionDescription)
        fun addIceCandidate(iceCandidate: IceCandidate)
    }
}