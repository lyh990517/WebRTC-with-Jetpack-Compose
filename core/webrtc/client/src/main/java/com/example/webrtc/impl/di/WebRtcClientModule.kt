package com.example.webrtc.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WebRtcClientModule {

    @Binds
    abstract fun bindsWebRtcClient(webRtcClient: com.example.webrtc.impl.WebRtcClient): com.example.webrtc.api.WebRtcClient
}
