package com.example.firestore.di

import com.example.webrtc.impl.Signaling
import com.example.firestore.SignalingImpl
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