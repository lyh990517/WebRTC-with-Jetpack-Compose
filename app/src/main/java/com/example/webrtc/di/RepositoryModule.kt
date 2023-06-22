package com.example.webrtc.di

import com.example.webrtc.data.SignalRepository
import com.example.webrtc.data.SignalRepositoryImpl
import com.example.webrtc.data.WebRTCRepository
import com.example.webrtc.data.WebRTCRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindsSignalRepository(signalRepositoryImpl: SignalRepositoryImpl): SignalRepository

    @Binds
    abstract fun bindsWebRTCRepository(webRTCRepositoryImpl: WebRTCRepositoryImpl): WebRTCRepository
}