package com.example.webrtc

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.*

class WebRTCClient(
    context: Application,
    private val observer: PeerConnection.Observer
) {
    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
        private const val FIELD_TRIALS = "WebRTC-H264HighProfile/Enabled/"
        private const val ICE_SERVER_URL = "stun:stun.l.google.com:19302"
    }

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    private val database = Firebase.firestore

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }

    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    init {
        initPeerConnectionFactory(context)
    }

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
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
}