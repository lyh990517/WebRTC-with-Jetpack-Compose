package com.example.webrtc.client

import com.example.common.WebRtcEvent
import kotlinx.coroutines.flow.SharedFlow
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

sealed interface Controller {
    interface LocalResource {
        fun getLocalSurface(): SurfaceViewRenderer
        fun getRemoteSurface(): SurfaceViewRenderer
        fun getLocalMediaStream(): MediaStream
        fun sinkToRemoteSurface(remoteMediaStream: MediaStream)
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
        fun getEvent(): SharedFlow<WebRtcEvent>
    }
}