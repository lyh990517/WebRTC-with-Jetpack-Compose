package com.example.data.client

import android.app.Application
import com.example.domain.LocalSurface
import com.example.domain.RemoteSurface
import com.example.domain.client.WebRtcClient
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

//TODO peerConnection 사용 유무 기준으로 분리

@Singleton
internal class WebRtcClientImpl @Inject constructor(
    private val application: Application,
    private val signalingManager: SignalingManager,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val peerConnectionManager: PeerConnectionManager,
    private val rootEglBase: EglBase,
    @RemoteSurface private val remoteSurface: SurfaceViewRenderer,
    @LocalSurface private val localSurface: SurfaceViewRenderer,
) : WebRtcClient {

    private var localAudioTrack: AudioTrack? = null

    private var localVideoTrack: VideoTrack? = null

    private lateinit var peerConnection: PeerConnection

    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    private val surfaceTextureHelper =
        SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)

    override fun connect(roomID: String, isHost: Boolean) {
        initializeClient(roomID, isHost)

        signalingManager.startSignaling(isHost, roomID)
    }

    override fun toggleVoice() {
        localAudioTrack?.setEnabled(localAudioTrack?.enabled()?.not() ?: false)
    }

    override fun toggleVideo() {
        localVideoTrack?.setEnabled(localVideoTrack?.enabled()?.not() ?: false)
    }

    override fun disconnect() {
        localAudioTrack?.dispose()
        localVideoTrack?.dispose()
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

    private fun getVideoCapture() =
        Camera2Enumerator(application).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    private fun initializeVideoCapture() {
        val videoCapture = getVideoCapture() as VideoCapturer

        videoCapture.initialize(
            surfaceTextureHelper,
            localSurface.context,
            localVideoSource.capturerObserver
        )

        videoCapture.startCapture(320, 240, 60)
    }

    private fun initializeLocalStream() {
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)

        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)

        peerConnection.addStream(localStream)
    }

    private fun initializeLocalResourceTracks() {
        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", localAudioSource)
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)

        localVideoTrack?.addSink(localSurface)
    }

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }
}
