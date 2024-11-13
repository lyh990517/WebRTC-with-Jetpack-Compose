package com.example.data.client

import android.app.Application
import com.example.domain.LocalSurface
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
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
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
    @RemoteSurface private val remoteView: SurfaceViewRenderer,
    @LocalSurface private val localView: SurfaceViewRenderer
) : WebRTCClient {
    private val webRtcScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack: AudioTrack? = null

    private var localVideoTrack: VideoTrack? = null

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }

    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    private val surfaceTextureHelper =
        SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)

    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private lateinit var peerConnection: PeerConnection

    override fun connect(roomID: String, isJoin: Boolean) {
        initialize(roomID, isJoin)

        if (!isJoin) sendOffer(roomID)

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

    private fun initialize(roomID: String, isJoin: Boolean) {
        initializePeerConnectionFactory()
        initializePeerConnection(isJoin, roomID)
        initializeSurfaceView(remoteView)
        initializeSurfaceView(localView)
        initializeVideoCapture()
        initializeLocalStream()
    }

    private fun collectFireStoreUpdate(roomID: String) {
        webRtcScope.launch {
            fireStoreRepository.connectToRoom(roomID).collect { data ->
                when {
                    data.containsKey("type") && data.getValue("type")
                        .toString() == "OFFER" -> handleOfferReceived(data, roomID)

                    data.containsKey("type") && data.getValue("type")
                        .toString() == "ANSWER" -> handleAnswerReceived(data)

                    else -> handleIceCandidateReceived(data)
                }
            }
        }
    }

    private fun initializePeerConnection(isJoin: Boolean, roomID: String) {
        peerConnectionFactory.createPeerConnection(
            iceServer,
            createPeerConnectionObserver(isJoin, roomID)
        )?.let { connection ->
            peerConnection = connection
        }
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

    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true)
            .setFieldTrials(FIELD_TRIALS)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory() = PeerConnectionFactory.builder().apply {
        setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
        setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
        setOptions(PeerConnectionFactory.Options().apply {
            disableEncryption = true
            disableNetworkMonitor = true
        })
    }.createPeerConnectionFactory()

    private fun createPeerConnectionObserver(
        isJoin: Boolean,
        roomID: String
    ): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let {
                    fireStoreRepository.sendIceCandidateToRoom(it, isJoin, roomID)
                    peerConnection.addIceCandidate(it)
                }
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {
                p0?.let { mediaStream ->
                    mediaStream.videoTracks?.get(0)?.addSink(remoteView)
                }
            }

            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
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

    private fun handleIceCandidateReceived(data: Map<String, Any>) {
        val iceCandidate = IceCandidate(
            data["sdpMid"].toString(),
            Math.toIntExact(data["sdpMLineIndex"] as Long),
            data["sdpCandidate"].toString()
        )

        peerConnection.addIceCandidate(iceCandidate)
    }

    private fun handleAnswerReceived(data: Map<String, Any>) {
        val sdp = SessionDescription(
            SessionDescription.Type.ANSWER,
            data["sdp"].toString()
        )
        val sdpObserver = createSdpObserver()

        peerConnection.setRemoteDescription(sdpObserver, sdp)
    }

    private fun handleOfferReceived(data: Map<String, Any>, roomID: String) {
        val sdp = SessionDescription(
            SessionDescription.Type.OFFER,
            data["sdp"].toString()
        )
        val sdpObserver = createSdpObserver()

        peerConnection.setRemoteDescription(sdpObserver, sdp)

        sendAnswer(roomID)
    }

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
        private const val FIELD_TRIALS = "WebRTC-H264HighProfile/Enabled/"
        private const val ICE_SERVER_URL = "stun:stun4.l.google.com:19302"
    }
}
