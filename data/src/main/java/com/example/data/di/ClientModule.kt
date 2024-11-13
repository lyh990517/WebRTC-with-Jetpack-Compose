package com.example.data.di

import com.example.data.client.WebRtcClientImpl
import com.example.domain.client.WebRtcClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ClientModule {

    @Binds
    abstract fun provideWebRTCClient(webRTCClient: WebRtcClientImpl): WebRtcClient
}
