package com.example.interactor

import android.content.Context
import com.example.domain.usecase.webrtc.SignalEvent
import com.example.domain.usecase.webrtc.WebRTCUseCase
import com.example.interactor.common.BaseCoroutineScope
import com.example.interactor.ext.Mapper.toInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

internal class WebRTCInteractorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webRTCUseCase: WebRTCUseCase,
) : WebRTCInteractor, BaseCoroutineScope() {

    private val _state = MutableStateFlow(WebRTCState())
    override val webRTCStateFlow: StateFlow<WebRTCState>
        get() = _state.asStateFlow()

    private val rootEglBase: EglBase = EglBase.create()
    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private lateinit var videoCapture: CameraVideoCapturer
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null


    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private val peerConnection by lazy { buildPeerConnection() }

    override val load: (initState: WebRTCState) -> Unit
        get() = { initState ->
            initializeWebRTC(initState)
            observeSignalEvents(initState)
        }

    override fun close() {
        localAudioTrack?.dispose()
        localVideoTrack?.dispose()
        peerConnection?.close()
    }

    private fun initializeWebRTC(initState: WebRTCState) {
        initPeerConnectionFactory()
        initVideoCapture()
        initState.apply {
            localSurfaceView?.let { view ->
                initSurfaceView(view)
                startLocalView(view)
            }
            remoteSurfaceView?.let(::initSurfaceView)
        }
        _state.value = initState
        if (!initState.isJoin) {
            peerConnection?.startCall(initState.accessCode)
        }
    }

    private fun observeSignalEvents(initState: WebRTCState) {
        webRTCUseCase.getSignalEventUseCase(initState.accessCode)
            .onEach { event ->
                processSignalEvent(event, initState.accessCode, initState.isJoin)
            }
            .launchIn(this)
    }

    private fun processSignalEvent(event: SignalEvent, accessCode: String, isJoin: Boolean) {
        when (event) {
            is SignalEvent.AnswerReceived -> handleAnswerReceived(event, isJoin)
            is SignalEvent.IceCandidateReceived -> handleIceCandidateReceived(event)
            is SignalEvent.OfferReceived -> handleOfferReceived(event, isJoin, accessCode)
        }
    }

    private fun handleAnswerReceived(event: SignalEvent.AnswerReceived, isJoin: Boolean) {
        if (!isJoin) {
            peerConnection?.setRemoteDescription(
                createSdpObserver { sessionDescription, sdpObserver -> },
                event.data
            )
        }
    }

    private fun handleIceCandidateReceived(event: SignalEvent.IceCandidateReceived) {
        peerConnection?.addIceCandidate(event.data)
    }

    private fun handleOfferReceived(
        event: SignalEvent.OfferReceived,
        isJoin: Boolean,
        accessCode: String
    ) {
        if (isJoin) {
            peerConnection?.apply {
                setRemoteDescription(
                    createSdpObserver { sessionDescription, sdpObserver -> },
                    event.data
                )
                createAnswer(
                    createSdpObserver { sdp, observer -> setLocalSdp(observer, sdp, accessCode) },
                    constraints
                )
            }
        }
    }

    private fun buildPeerConnectionFactory() = PeerConnectionFactory.builder().apply {
        setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
        setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
        setOptions(PeerConnectionFactory.Options().apply {
            disableEncryption = true
            disableNetworkMonitor = true
        })
    }.createPeerConnectionFactory()

    private fun PeerConnection.startCall(accessCode: String) {
        createOffer(
            createSdpObserver { sdp, observer -> setLocalSdp(observer, sdp, accessCode) },
            constraints
        )
    }

    private fun initPeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials(FIELD_TRIALS)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }


    private fun createPeerConnectionObserver(): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let { data ->
                    val currentState = _state.value
                    webRTCUseCase.sendIceCandidateUseCase(
                        currentState.accessCode,
                        data.toInfo(currentState.isJoin)
                    ).launchIn(this@WebRTCInteractorImpl)
                }
            }

            override fun onAddStream(stream: MediaStream?) {
                stream?.videoTracks?.firstOrNull()?.let { videoTrack ->
                    _state.value = _state.value.copy(
                        remoteSurfaceView = _state.value.remoteSurfaceView?.also { view ->
                            videoTrack.addSink(view)
                        }
                    )
                }
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        }


    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())


    private fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    private fun getVideoCapture(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    private fun buildPeerConnection() =
        peerConnectionFactory.createPeerConnection(iceServer, createPeerConnectionObserver())

    private fun initVideoCapture() {
        videoCapture = getVideoCapture(context)
    }


    private fun startLocalView(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapture as VideoCapturer).initialize(
            surfaceTextureHelper,
            localVideoOutput.context,
            localVideoSource.capturerObserver
        )
        videoCapture.startCapture(320, 240, 60)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource)
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack?.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }

    private fun setLocalSdp(
        observer: SdpObserver,
        sdp: SessionDescription,
        accessCode: String
    ) {
        peerConnection?.setLocalDescription(observer, sdp)
        webRTCUseCase.sendSdpUseCase(accessCode, sdp).launchIn(this)
    }

    private fun createSdpObserver(setLocalDescription: ((SessionDescription, SdpObserver) -> Unit)?) =
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
        private const val FIELD_TRIALS = "WebRTC-H264HighProfile/Enabled/"
        private const val ICE_SERVER_URL = "stun:stun4.l.google.com:19302"
    }
}
