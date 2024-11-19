package com.example.webrtc.client.di

import android.app.Application
import android.content.Context
import com.example.webrtc.client.di.qualifier.LocalSurface
import com.example.webrtc.client.di.qualifier.RemoteSurface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object WebRtcComponentModule {

    private const val LOCAL_VIDEO_TRACK = "local_track_video"
    private const val LOCAL_AUDIO_TRACK = "local_track_audio"
    private const val FIELD_TRIAL = "WebRTC-H264HighProfile/Enabled/"

    @Provides
    @Singleton
    fun providesRootEglBase(): EglBase = EglBase.create()

    @Provides
    @Singleton
    @RemoteSurface
    fun providesRemoveSurface(
        @ApplicationContext context: Context,
        rootEglBase: EglBase,
    ): SurfaceViewRenderer {
        val remoteSurface = SurfaceViewRenderer(context)

        initializeSurfaceView(remoteSurface, rootEglBase)

        return remoteSurface
    }

    @Provides
    @Singleton
    @LocalSurface
    fun providesLocalSurface(
        @ApplicationContext context: Context,
        rootEglBase: EglBase,
    ): SurfaceViewRenderer {
        val localSurface = SurfaceViewRenderer(context)

        initializeSurfaceView(localSurface, rootEglBase)

        return localSurface
    }

    private fun initializeSurfaceView(
        view: SurfaceViewRenderer,
        rootEglBase: EglBase,
    ) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    @Provides
    @Singleton
    fun providesPeerConnectionFactory(
        application: Application,
        rootEglBase: EglBase,
    ): PeerConnectionFactory {
        val options = PeerConnectionFactory.InitializationOptions.builder(application)
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

    @Provides
    @Singleton
    fun providesVideoSource(peerConnectionFactory: PeerConnectionFactory): VideoSource =
        peerConnectionFactory.createVideoSource(false)

    @Provides
    @Singleton
    fun providesAudioSource(peerConnectionFactory: PeerConnectionFactory): AudioSource =
        peerConnectionFactory.createAudioSource(MediaConstraints())

    @Provides
    @Singleton
    fun providesVideoTrack(
        @LocalSurface localSurface: SurfaceViewRenderer,
        peerConnectionFactory: PeerConnectionFactory,
        videoSource: VideoSource,
    ): VideoTrack {
        val localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_VIDEO_TRACK, videoSource)

        localVideoTrack.addSink(localSurface)

        return localVideoTrack
    }

    @Provides
    @Singleton
    fun providesAudioTrack(
        peerConnectionFactory: PeerConnectionFactory,
        audioSource: AudioSource,
    ): AudioTrack =
        peerConnectionFactory.createAudioTrack(LOCAL_AUDIO_TRACK, audioSource)

    @Provides
    @Singleton
    fun providesSurfaceTextureHelper(rootEglBase: EglBase): SurfaceTextureHelper =
        SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)

    @Provides
    @Singleton
    fun providesVideoCapturer(
        application: Application,
        @LocalSurface localSurface: SurfaceViewRenderer,
        surfaceTextureHelper: SurfaceTextureHelper,
        localVideoSource: VideoSource,
    ): VideoCapturer {
        val videoCapturer = Camera2Enumerator(application).run {
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
}
