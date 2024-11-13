package com.example.data.di

import android.app.Application
import com.example.domain.LocalSurface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PeerConnectionModule {

    @Provides
    @Singleton
    fun providesRootEglBase(): EglBase = EglBase.create()

    @Provides
    @Singleton
    fun providesPeerConnectionFactory(
        application: Application,
        rootEglBase: EglBase,
    ): PeerConnectionFactory {
        val options = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
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
        peerConnectionFactory: PeerConnectionFactory,
        videoSource: VideoSource,
    ): VideoTrack =
        peerConnectionFactory.createVideoTrack("local_track", videoSource)

    @Provides
    @Singleton
    fun providesAudioTrack(
        peerConnectionFactory: PeerConnectionFactory,
        audioSource: AudioSource,
    ): AudioTrack =
        peerConnectionFactory.createAudioTrack("local_track_audio", audioSource)

    @Provides
    @Singleton
    fun providesMediaStream(
        peerConnectionFactory: PeerConnectionFactory,
        videoTrack: VideoTrack,
        audioTrack: AudioTrack,
    ): MediaStream {
        val mediaStream = peerConnectionFactory.createLocalMediaStream("local_track")

        mediaStream.addTrack(videoTrack)
        mediaStream.addTrack(audioTrack)

        return mediaStream
    }

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
