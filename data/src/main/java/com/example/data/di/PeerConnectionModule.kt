package com.example.data.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PeerConnectionModule {

    @Provides
    @Singleton
    fun providesRootEglBase() = EglBase.create()

    @Provides
    @Singleton
    fun providesPeerConnectionFactory(
        application: Application,
        rootEglBase: EglBase
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
}