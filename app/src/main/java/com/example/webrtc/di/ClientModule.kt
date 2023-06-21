package com.example.webrtc.di

import com.example.webrtc.client.SignalingClient
import com.example.webrtc.data.WebRTCRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClientModule {

        @Provides
    @Singleton
    fun providesSignalingClient(webRTCRepository: WebRTCRepository) = SignalingClient(webRTCRepository)
}