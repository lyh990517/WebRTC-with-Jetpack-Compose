package com.example.webrtc.sdk.controller

import com.example.webrtc.sdk.controller.DataChannelManager
import com.example.webrtc.sdk.event.WebRtcEvent
import com.example.webrtc.sdk.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.merge
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

// TODO Turn 서버 지정

internal class WebRtcController(
    private val peerConnectionManager: PeerConnectionManager,
    private val localResourceManager: LocalResourceManager,
    private val dataChannelManager: DataChannelManager,
) {
    private val controllerEvent = MutableSharedFlow<WebRtcEvent>(extraBufferCapacity = 100)

    fun initialize(isHost: Boolean) {
        peerConnectionManager.initialize(isHost)

        val dataChannel = peerConnectionManager.createDataChannel()

        dataChannelManager.initialize(dataChannel)

        peerConnectionManager.initializeLocalResource(
            videoTrack = localResourceManager.getVideoTrack(),
            audioTrack = localResourceManager.getAudioTrack()
        )

        if (isHost) {
            controllerEvent.tryEmit(WebRtcEvent.Host.SendOffer)
        }
    }

    fun sendMessage(message: String) {
        dataChannelManager.sendMessage(message)
    }

    fun sendInputEvent() {
        dataChannelManager.sendInputEvent()
    }

    fun getMessages(): Flow<Message> =
        dataChannelManager.getMessages()


    fun createOffer() {
        peerConnectionManager.createOffer()
    }

    fun createAnswer() {
        peerConnectionManager.createAnswer()
    }

    fun closeConnection() {
        peerConnectionManager.closeConnection()
    }

    fun sinkVideoTrack(videoTrack: VideoTrack) {
        videoTrack.addSink(localResourceManager.getRemoteSurface())
    }

    fun setRemoteDescription(sdp: SessionDescription) {
        peerConnectionManager.setRemoteDescription(sdp)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnectionManager.addIceCandidate(iceCandidate)
    }

    fun getLocalSurface(): SurfaceViewRenderer =
        localResourceManager.getLocalSurface()

    fun getRemoteSurface(): SurfaceViewRenderer =
        localResourceManager.getRemoteSurface()

    fun toggleVoice() {
        localResourceManager.toggleVoice()
    }

    fun toggleVideo() {
        localResourceManager.toggleVideo()
    }

    fun startCapture() {
        localResourceManager.startCapture()
    }

    fun dispose() {
        localResourceManager.dispose()
    }

    fun getEvent(): Flow<WebRtcEvent> = merge(
        controllerEvent.asSharedFlow(),
        peerConnectionManager.getEvent(),
        dataChannelManager.getEvent()
    )
}
