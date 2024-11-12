package com.example.data.di

import com.example.data.client.WebRTCClientImpl
import com.example.domain.client.WebRTCClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ClientModule {

    @Binds
    abstract fun provideWebRTCClient(webRTCClient: WebRTCClientImpl): WebRTCClient
}