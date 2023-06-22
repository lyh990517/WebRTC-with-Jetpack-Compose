package com.example.webrtc.di

import android.app.Application
import com.example.webrtc.client.SignalingClient
import com.example.webrtc.client.WebRTCClient
import com.example.webrtc.data.SignalRepository
import com.example.webrtc.data.WebRTCRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClientModule {

    @Provides
    @Singleton
    fun providesSignalingClient(signalRepository: SignalRepository) =
        SignalingClient(signalRepository)

    @Provides
    @Singleton
    fun provideWebRTCClient(
        webRTCRepository: WebRTCRepository, ) = WebRTCClient(webRTCRepository)
}