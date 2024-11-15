package com.example.webrtc.impl.di

import com.example.webrtc.api.WebRtcClient
import com.example.webrtc.impl.WebRtcClientImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WebRtcClientModule {

    @Binds
    abstract fun bindsWebRtcClient(webRtcClientImpl: WebRtcClientImpl): WebRtcClient
}
