package com.example.webrtc.impl.di

import com.example.webrtc.api.FireStoreRepository
import com.example.webrtc.impl.FireStoreRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    abstract fun bindsFireStoreRepository(fireStoreRepositoryImpl: FireStoreRepositoryImpl): FireStoreRepository
}
