package com.example.webrtc.sdk.factory

import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.webrtc.sdk.WebRtcClientImpl
import com.example.webrtc.sdk.api.WebRtcClient
import com.example.webrtc.sdk.controller.DataChannelManager
import com.example.webrtc.sdk.controller.LocalResourceManager
import com.example.webrtc.sdk.controller.PeerConnectionManager
import com.example.webrtc.sdk.controller.WebRtcController
import com.example.webrtc.sdk.event.EventHandler
import com.example.webrtc.sdk.signaling.IceManager
import com.example.webrtc.sdk.signaling.SdpManager
import com.example.webrtc.sdk.signaling.SignalingImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object WebRtcClientFactory {
    fun create(context: Context): WebRtcClient {
        val webrtcScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val rootEglBase = WebRtcComponentProvider.createRootEglBase()
        val peerConnectionFactory = WebRtcComponentProvider.createPeerConnectionFactory(
            context,
            rootEglBase
        )
        val audioSource = WebRtcComponentProvider.createAudioSource(peerConnectionFactory)
        val videoSource = WebRtcComponentProvider.createVideoSource(peerConnectionFactory, false)
        val localSurface = WebRtcComponentProvider.createSurfaceViewRenderer(context, rootEglBase)
        val remoteSurface = WebRtcComponentProvider.createSurfaceViewRenderer(context, rootEglBase)
        val videoTrack =
            WebRtcComponentProvider.createVideoTrack(
                localSurface,
                peerConnectionFactory,
                videoSource
            )
        val surfaceTextureHelper = WebRtcComponentProvider.createSurfaceTextureHelper(rootEglBase)
        val audioTrack =
            WebRtcComponentProvider.createAudioTrack(peerConnectionFactory, audioSource)
        val videoCapturer = WebRtcComponentProvider.createVideoCapturerWithCamera(
            context,
            localSurface,
            surfaceTextureHelper,
            videoSource
        )
        val localResourceManager =
            LocalResourceManager(
                localSurface = localSurface,
                remoteSurface = remoteSurface,
                localVideoTrack = videoTrack,
                localAudioTrack = audioTrack,
                videoCapturer = videoCapturer
            )
        val firestore = Firebase.firestore
        val sdpManager = SdpManager(firestore, webrtcScope)
        val iceManager = IceManager(firestore, webrtcScope)
        val signaling = SignalingImpl(firestore, sdpManager, iceManager)
        val dataChannelManager = DataChannelManager(webrtcScope)
        val peerConnectionManager = PeerConnectionManager(peerConnectionFactory)
        val webRtcController =
            WebRtcController(
                localResourceManager = localResourceManager,
                dataChannelManager = dataChannelManager,
                peerConnectionManager = peerConnectionManager
            )
        val eventHandler = EventHandler(webRtcController, signaling)

        return WebRtcClientImpl(
            webRtcScope = webrtcScope,
            eventHandler = eventHandler,
            webRtcController = webRtcController,
            signaling = signaling,
        )
    }

    fun create(context: Context, mediaProjectionIntent: Intent): WebRtcClient {
        val webrtcScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val rootEglBase = WebRtcComponentProvider.createRootEglBase()
        val peerConnectionFactory = WebRtcComponentProvider.createPeerConnectionFactory(
            context,
            rootEglBase
        )
        val audioSource = WebRtcComponentProvider.createAudioSource(peerConnectionFactory)
        val videoSource = WebRtcComponentProvider.createVideoSource(peerConnectionFactory, true)
        val localSurface = WebRtcComponentProvider.createSurfaceViewRenderer(context, rootEglBase)
        val remoteSurface = WebRtcComponentProvider.createSurfaceViewRenderer(context, rootEglBase)
        val videoTrack =
            WebRtcComponentProvider.createVideoTrack(
                localSurface,
                peerConnectionFactory,
                videoSource
            )
        val surfaceTextureHelper = WebRtcComponentProvider.createSurfaceTextureHelper(rootEglBase)
        val audioTrack =
            WebRtcComponentProvider.createAudioTrack(peerConnectionFactory, audioSource)
        val videoCapturer = WebRtcComponentProvider.createVideoCapturerWithMediaProjection(
            context,
            mediaProjectionIntent = mediaProjectionIntent,
            surfaceTextureHelper = surfaceTextureHelper,
            localVideoSource = videoSource,
            videoTrack = videoTrack,
            localSurface = localSurface
        )

        val localResourceManager =
            LocalResourceManager(
                localSurface = localSurface,
                remoteSurface = remoteSurface,
                localVideoTrack = videoTrack,
                localAudioTrack = audioTrack,
                videoCapturer = videoCapturer
            )
        val firestore = Firebase.firestore
        val sdpManager = SdpManager(firestore, webrtcScope)
        val iceManager = IceManager(firestore, webrtcScope)
        val signaling = SignalingImpl(firestore, sdpManager, iceManager)
        val dataChannelManager = DataChannelManager(webrtcScope)
        val peerConnectionManager = PeerConnectionManager(peerConnectionFactory)
        val webRtcController =
            WebRtcController(
                localResourceManager = localResourceManager,
                dataChannelManager = dataChannelManager,
                peerConnectionManager = peerConnectionManager
            )
        val eventHandler = EventHandler(webRtcController, signaling)

        return WebRtcClientImpl(
            webRtcScope = webrtcScope,
            eventHandler = eventHandler,
            webRtcController = webRtcController,
            signaling = signaling,
        )
    }
}
