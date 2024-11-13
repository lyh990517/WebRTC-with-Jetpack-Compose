package com.example.data.client

import com.example.domain.LocalSurface
import com.example.domain.RemoteSurface
import com.example.domain.client.WebRtcClient
import org.webrtc.AudioTrack
import org.webrtc.EglBase
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WebRtcClientImpl @Inject constructor(
    @RemoteSurface private val remoteSurface: SurfaceViewRenderer,
    @LocalSurface private val localSurface: SurfaceViewRenderer,
    private val signalingManager: SignalingManager,
    private val peerConnectionManager: PeerConnectionManager,
    private val rootEglBase: EglBase,
    private val localMediaStream: MediaStream,
    private val localAudioTrack: AudioTrack,
    private val localVideoTrack: VideoTrack,
    private val videoCapturer: VideoCapturer,
) : WebRtcClient {

    private lateinit var peerConnection: PeerConnection

    override fun connect(roomID: String, isHost: Boolean) {
        initializeClient(roomID, isHost)

        signalingManager.startSignaling(isHost, roomID)
    }

    override fun toggleVoice() {
        localAudioTrack.setEnabled(!localAudioTrack.enabled())
    }

    override fun toggleVideo() {
        localVideoTrack.setEnabled(!localVideoTrack.enabled())
    }

    override fun disconnect() {
        localAudioTrack.dispose()
        localVideoTrack.dispose()
        peerConnection.close()
        signalingManager.close()
    }

    private fun initializeClient(roomID: String, isHost: Boolean) {
        initializePeerConnection(isHost, roomID)
        initializeSurfaceView(remoteSurface)
        initializeSurfaceView(localSurface)
        initializeVideoCapture()
        initializeLocalResourceTracks()
        initializeLocalStream()
    }

    private fun initializePeerConnection(isHost: Boolean, roomID: String) {
        peerConnectionManager.initializePeerConnection(isHost, roomID)

        peerConnection = peerConnectionManager.getPeerConnection()
    }

    private fun initializeSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    private fun initializeVideoCapture() {
        videoCapturer.startCapture(320, 240, 60)
    }

    private fun initializeLocalStream() {
        peerConnection.addStream(localMediaStream)
    }

    private fun initializeLocalResourceTracks() {
        localVideoTrack.addSink(localSurface)
    }
}
