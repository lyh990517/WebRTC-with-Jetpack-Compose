package com.example.event.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides
    @Singleton
    fun providesDispatcher() = Dispatchers.IO

    @Provides
    @Singleton
    fun provideWebRtcScope(dispatcher: CoroutineDispatcher) =
        CoroutineScope(dispatcher + SupervisorJob())
}