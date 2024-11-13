package com.example.domain.client

import android.app.Application
import android.content.Context
import kotlinx.coroutines.Job
import org.webrtc.CameraVideoCapturer
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

interface WebRTCClient {
    fun getVideoCapture(context: Context): CameraVideoCapturer

    fun initSurfaceView(view: SurfaceViewRenderer)

    fun startLocalView()

    fun initVideoCapture(context: Application)

    fun initPeerConnectionFactory(context: Application)

    fun buildPeerConnectionFactory(): PeerConnectionFactory

    fun createPeerConnectionObserver(): PeerConnection.Observer

    fun buildPeerConnection(): PeerConnection?

    fun PeerConnection.call(roomID: String)

    fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean, roomID: String)

    fun PeerConnection.answer(roomID: String)

    fun setLocalSdp(
        observer: SdpObserver,
        sdp: SessionDescription,
        roomID: String
    )

    fun onRemoteSessionReceived(description: SessionDescription)

    fun createSdpObserver(setLocalDescription: ((SessionDescription, SdpObserver) -> Unit)? = null): SdpObserver

    fun addCandidate(iceCandidate: IceCandidate?)

    fun answer(roomID: String): Unit?

    fun call(roomID: String): Unit?

    fun toggleVoice()

    fun toggleVideo()

    fun closeSession()

    fun connectToRoom(roomID: String)

    fun connect(roomID: String, isJoin: Boolean): Job

    fun handleIceCandidateReceived(data: Map<String, Any>)

    fun handleAnswerReceived(data: Map<String, Any>)

    fun handleOfferReceived(data: Map<String, Any>, roomID: String)
}