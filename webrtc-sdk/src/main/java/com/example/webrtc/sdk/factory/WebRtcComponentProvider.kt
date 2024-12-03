package com.example.webrtc.sdk.factory

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

internal object WebRtcComponentProvider {

    private const val LOCAL_VIDEO_TRACK = "local_track_video"
    private const val LOCAL_AUDIO_TRACK = "local_track_audio"
    private const val FIELD_TRIAL = "WebRTC-H264HighProfile/Enabled/"

    fun createRootEglBase(): EglBase = EglBase.create()

    fun createSurfaceViewRenderer(
        context: Context,
        rootEglBase: EglBase,
    ): SurfaceViewRenderer {
        val remoteSurface = SurfaceViewRenderer(context)

        initializeSurfaceView(remoteSurface, rootEglBase)

        return remoteSurface
    }

    private fun initializeSurfaceView(
        view: SurfaceViewRenderer,
        rootEglBase: EglBase,
    ) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    fun createPeerConnectionFactory(
        context: Context,
        rootEglBase: EglBase,
    ): PeerConnectionFactory {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials(FIELD_TRIAL)
            .createInitializationOptions()

        PeerConnectionFactory.initialize(options)

        return PeerConnectionFactory.builder().apply {
            setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    rootEglBase.eglBaseContext,
                    true,
                    true
                )
            )
            setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            })
        }.createPeerConnectionFactory()
    }

    fun createVideoSource(
        peerConnectionFactory: PeerConnectionFactory,
        isScreenCast: Boolean
    ): VideoSource =
        peerConnectionFactory.createVideoSource(isScreenCast)

    fun createAudioSource(peerConnectionFactory: PeerConnectionFactory): AudioSource =
        peerConnectionFactory.createAudioSource(MediaConstraints())

    fun createVideoTrack(
        localSurface: SurfaceViewRenderer,
        peerConnectionFactory: PeerConnectionFactory,
        videoSource: VideoSource,
    ): VideoTrack {
        val localVideoTrack = peerConnectionFactory.createVideoTrack(
            LOCAL_VIDEO_TRACK,
            videoSource
        )

        localVideoTrack.addSink(localSurface)

        return localVideoTrack
    }

    fun createAudioTrack(
        peerConnectionFactory: PeerConnectionFactory,
        audioSource: AudioSource,
    ): AudioTrack =
        peerConnectionFactory.createAudioTrack(LOCAL_AUDIO_TRACK, audioSource)

    fun createSurfaceTextureHelper(rootEglBase: EglBase): SurfaceTextureHelper =
        SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)

    fun createVideoCapturerWithCamera(
        context: Context,
        localSurface: SurfaceViewRenderer,
        surfaceTextureHelper: SurfaceTextureHelper,
        localVideoSource: VideoSource,
    ): VideoCapturer {
        val videoCapturer = Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        } as VideoCapturer

        videoCapturer.initialize(
            surfaceTextureHelper,
            localSurface.context,
            localVideoSource.capturerObserver
        )

        return videoCapturer
    }

    fun createVideoCapturerWithMediaProjection(
        context: Context,
        mediaProjectionIntent: Intent,
        surfaceTextureHelper: SurfaceTextureHelper,
        localVideoSource: VideoSource,
        videoTrack: VideoTrack,
        localSurface: SurfaceViewRenderer
    ): VideoCapturer {
        return ScreenCapturerAndroid(
            mediaProjectionIntent, object : MediaProjection.Callback() {
                override fun onStop() {
                }
            }
        ).apply {
            initialize(surfaceTextureHelper, context, localVideoSource.capturerObserver)
            videoTrack.addSink(localSurface)
        }
    }
}
