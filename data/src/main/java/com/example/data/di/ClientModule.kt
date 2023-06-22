package com.example.data.di

import com.example.data.client.SignalingClientImpl
import com.example.data.client.WebRTCClientImpl
import com.example.domain.client.SignalingClient
import com.example.domain.client.WebRTCClient
import com.example.domain.repository.SignalRepository
import com.example.domain.repository.WebRTCRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ClientModule {

    @Binds
    abstract fun providesSignalingClient(signalingClient: SignalingClientImpl): SignalingClient

    @Binds
    abstract fun provideWebRTCClient(webRTCClient: WebRTCClientImpl): WebRTCClient
}