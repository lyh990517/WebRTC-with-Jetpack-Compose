package com.example.webrtc.client.controller

import com.example.webrtc.client.event.WebRtcEvent
import com.example.webrtc.client.model.Message
import kotlinx.coroutines.flow.Flow
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

sealed interface Controller {
    interface LocalResource {
        fun getLocalSurface(): SurfaceViewRenderer
        fun getRemoteSurface(): SurfaceViewRenderer
        fun getVideoTrack(): VideoTrack
        fun getAudioTrack(): AudioTrack
        fun sinkToRemoteSurface(remoteMediaStream: MediaStream)
        fun toggleVoice()
        fun toggleVideo()
        fun startCapture()
        fun dispose()
    }

    interface WebRtc {
        fun initialize(roomID: String, isHost: Boolean, speechModeOn: Boolean)
        fun sendMessage(message: String)
        fun sendInputEvent()
        fun getMessages(): Flow<Message>
        fun createOffer()
        fun createAnswer()
        fun closeConnection()
        fun setLocalDescription(sdp: SessionDescription, observer: SdpObserver)
        fun setRemoteDescription(sdp: SessionDescription)
        fun addIceCandidate(iceCandidate: IceCandidate)
        fun getEvent(): Flow<WebRtcEvent>
    }
}