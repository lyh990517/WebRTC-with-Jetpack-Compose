package com.example.data.client

import android.app.Application
import android.content.Context
import com.example.domain.client.WebRTCClient
import com.example.domain.repository.FireStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
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

internal class WebRTCClientImpl @Inject constructor(
    private val fireStoreRepository: FireStoreRepository,
) : WebRTCClient {
    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack: AudioTrack? = null

    private var localVideoTrack: VideoTrack? = null

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }

    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    private lateinit var videoCapture: CameraVideoCapturer

    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private val peerConnection by lazy { buildPeerConnection() }

    private val isMicEnabled = MutableStateFlow(true)
    private val isVideoEnabled = MutableStateFlow(true)

    private lateinit var dataFlow: Flow<Map<String, Any>>

    private var join: Boolean = false

    private var roomId = ""

    private lateinit var remoteSurfaceView: SurfaceViewRenderer

    init {
        collectState()
    }

    private fun collectState() {
        CoroutineScope(Dispatchers.IO).launch {
            isMicEnabled.collect {
                localAudioTrack?.setEnabled(it)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            isVideoEnabled.collect {
                localVideoTrack?.setEnabled(it)
            }
        }
    }

    override fun getVideoCapture(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    override fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    override fun startLocalView(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapture as VideoCapturer).initialize(
            surfaceTextureHelper,
            localVideoOutput.context,
            localVideoSource.capturerObserver
        )
        videoCapture.startCapture(320, 240, 60)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource);
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack?.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }

    override fun initVideoCapture(context: Application) {
        videoCapture = getVideoCapture(context)
    }

    override fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials(FIELD_TRIALS)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    override fun buildPeerConnectionFactory() = PeerConnectionFactory.builder().apply {
        setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
        setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
        setOptions(PeerConnectionFactory.Options().apply {
            disableEncryption = true
            disableNetworkMonitor = true
        })
    }.createPeerConnectionFactory()

    override fun createPeerConnectionObserver(): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: IceCandidate?) {
                CoroutineScope(Dispatchers.IO).launch {
                    p0?.let {
                        fireStoreRepository.sendIceCandidateToRoom(it, join, roomId)
                        peerConnection?.addIceCandidate(it)
                    }
                }
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {
                CoroutineScope(Dispatchers.IO).launch {
                    p0?.let { mediaStream ->
                        mediaStream.videoTracks?.get(0)?.addSink(remoteSurfaceView)
                    }
                }
            }

            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        }

    override fun buildPeerConnection() =
        peerConnectionFactory.createPeerConnection(iceServer, createPeerConnectionObserver())

    override fun PeerConnection.call(roomID: String) {
        createOffer(
            createSdpObserver() { sdp, observer -> setLocalSdp(observer, sdp, roomID) },
            constraints
        )
    }

    override fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean, roomID: String) =
        runBlocking {
            fireStoreRepository.sendIceCandidateToRoom(candidate, isJoin, roomID)
        }

    override fun PeerConnection.answer(roomID: String) {
        createAnswer(
            createSdpObserver() { sdp, observer -> setLocalSdp(observer, sdp, roomID) },
            constraints
        )
    }

    override fun setLocalSdp(
        observer: SdpObserver,
        sdp: SessionDescription,
        roomID: String
    ) {
        peerConnection?.setLocalDescription(observer, sdp)
        fireStoreRepository.sendSdpToRoom(sdp = sdp, roomId = roomID)
    }

    override fun onRemoteSessionReceived(description: SessionDescription) {
        peerConnection?.setRemoteDescription(createSdpObserver(), description)
    }

    override fun createSdpObserver(setLocalDescription: ((SessionDescription, SdpObserver) -> Unit)?) =
        object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                setLocalDescription?.let { function -> p0?.let { sdp -> function(sdp, this) } }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }

    override fun addCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    override fun toggleVoice() {
        isMicEnabled.value = !isMicEnabled.value
    }

    override fun toggleVideo() {
        isVideoEnabled.value = !isVideoEnabled.value
    }

    override fun closeSession() {
        localAudioTrack?.dispose()
        localVideoTrack?.dispose()
        peerConnection?.close()
    }

    override fun answer(roomID: String) =
        peerConnection?.answer(roomID)

    override fun call(roomID: String) =
        peerConnection?.call(roomID)

    override fun initialize(roomID: String) {
        dataFlow = fireStoreRepository.connectToRoom(roomID)
    }

    override fun connect(roomID: String, isJoin: Boolean, remoteView: SurfaceViewRenderer) =
        CoroutineScope(Dispatchers.IO).launch {
            join = isJoin
            roomId = roomID
            remoteSurfaceView = remoteView

            dataFlow.collect { data ->
                when {
                    data.containsKey("type") && data.getValue("type")
                        .toString() == "OFFER" -> handleOfferReceived(data, roomID)

                    data.containsKey("type") && data.getValue("type")
                        .toString() == "ANSWER" -> handleAnswerReceived(data)

                    else -> handleIceCandidateReceived(data)
                }
            }
        }

    override fun handleIceCandidateReceived(data: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            peerConnection?.addIceCandidate(
                IceCandidate(
                    data["sdpMid"].toString(),
                    Math.toIntExact(data["sdpMLineIndex"] as Long),
                    data["sdpCandidate"].toString()
                )
            )
        }
    }

    override fun handleAnswerReceived(data: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            peerConnection?.setRemoteDescription(
                createSdpObserver(), SessionDescription(
                    SessionDescription.Type.ANSWER,
                    data["sdp"].toString()
                )
            )
        }
    }

    override fun handleOfferReceived(data: Map<String, Any>, roomID: String) {
        CoroutineScope(Dispatchers.IO).launch {
            peerConnection?.setRemoteDescription(
                createSdpObserver(), SessionDescription(
                    SessionDescription.Type.OFFER,
                    data["sdp"].toString()
                )
            )
            answer(roomID)
        }
    }

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
        private const val FIELD_TRIALS = "WebRTC-H264HighProfile/Enabled/"
        private const val ICE_SERVER_URL = "stun:stun4.l.google.com:19302"
    }
}
