package com.example.signaling.di

import com.example.webrtc.client.Signaling
import com.example.signaling.SignalingImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SignalingModule {

    @Binds
    abstract fun bindsSignaling(signalingImpl: SignalingImpl): Signaling
}