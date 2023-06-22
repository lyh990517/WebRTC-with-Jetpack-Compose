package com.example.data.di

import com.example.data.repository.SignalRepositoryImpl
import com.example.data.repository.WebRTCRepositoryImpl
import com.example.domain.repository.SignalRepository
import com.example.domain.repository.WebRTCRepository
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