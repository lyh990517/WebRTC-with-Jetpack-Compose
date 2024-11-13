package com.example.data.client

import android.app.Application
import com.example.domain.LocalSurface
import com.example.domain.Packet.Companion.isAnswer
import com.example.domain.Packet.Companion.isOffer
import com.example.domain.Packet.Companion.toAnswerSdp
import com.example.domain.Packet.Companion.toIceCandidate
import com.example.domain.Packet.Companion.toOfferSdp
import com.example.domain.RemoteSurface
import com.example.domain.client.WebRTCClient
import com.example.domain.repository.FireStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

//TODO peerConnection 사용 유무 기준으로 분리

@Singleton
internal class WebRTCClientImpl @Inject constructor(
    private val application: Application,
    private val fireStoreRepository: FireStoreRepository,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val peerConnectionManager: PeerConnectionManager,
    private val rootEglBase: EglBase,
    @RemoteSurface private val remoteView: SurfaceViewRenderer,
    @LocalSurface private val localView: SurfaceViewRenderer
) : WebRTCClient {
    private val webRtcScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private var localAudioTrack: AudioTrack? = null

    private var localVideoTrack: VideoTrack? = null

    private lateinit var peerConnection: PeerConnection

    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    private val surfaceTextureHelper =
        SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)

    override fun connect(roomID: String, isHost: Boolean) {
        initialize(roomID, isHost)

        if (isHost) sendOffer(roomID)

        collectFireStoreUpdate(roomID)
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
        webRtcScope.cancel()
    }

    private fun initialize(roomID: String, isHost: Boolean) {
        initializePeerConnection(isHost, roomID)
        initializeSurfaceView(remoteView)
        initializeSurfaceView(localView)
        initializeVideoCapture()
        initializeLocalStream()
    }

    private fun collectFireStoreUpdate(roomID: String) {
        webRtcScope.launch {
            fireStoreRepository.connectToRoom(roomID).collect { packet ->
                when {
                    packet.isOffer() -> {
                        val sdp = packet.toOfferSdp()
                        val sdpObserver = createSdpObserver()
                        peerConnection.setRemoteDescription(sdpObserver, sdp)

                        sendAnswer(roomID)
                    }

                    packet.isAnswer() -> {
                        val sdp = packet.toAnswerSdp()
                        val sdpObserver = createSdpObserver()

                        peerConnection.setRemoteDescription(sdpObserver, sdp)
                    }

                    else -> {
                        val iceCandidate = packet.toIceCandidate()

                        peerConnection.addIceCandidate(iceCandidate)
                    }
                }
            }
        }
    }

    private fun initializePeerConnection(isHost: Boolean, roomID: String) {
        peerConnectionManager.initializePeerConnection(isHost, roomID)

        peerConnection = peerConnectionManager.getPeerConnection()
    }

    private fun getVideoCapture() =
        Camera2Enumerator(application).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    private fun initializeSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    private fun initializeLocalStream() {
        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", localAudioSource)
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)

        localVideoTrack?.addSink(localView)

        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)

        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)

        peerConnection.addStream(localStream)
    }

    private fun initializeVideoCapture() {
        val videoCapture = getVideoCapture() as VideoCapturer

        videoCapture.initialize(
            surfaceTextureHelper,
            localView.context,
            localVideoSource.capturerObserver
        )

        videoCapture.startCapture(320, 240, 60)
    }

    private fun sendOffer(roomID: String) {
        val sdpObserver = createSdpObserver { sdp, observer ->
            peerConnection.setLocalDescription(observer, sdp)
            fireStoreRepository.sendSdpToRoom(sdp = sdp, roomId = roomID)
        }

        peerConnection.createOffer(sdpObserver, constraints)
    }

    private fun sendAnswer(roomID: String) {
        val sdpObserver = createSdpObserver { sdp, observer ->
            peerConnection.setLocalDescription(observer, sdp)
            fireStoreRepository.sendSdpToRoom(sdp = sdp, roomId = roomID)
        }

        peerConnection.createAnswer(sdpObserver, constraints)
    }

    private fun createSdpObserver(setLocalDescription: ((SessionDescription, SdpObserver) -> Unit)? = null) =
        object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                setLocalDescription?.let { function -> p0?.let { sdp -> function(sdp, this) } }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }
}
