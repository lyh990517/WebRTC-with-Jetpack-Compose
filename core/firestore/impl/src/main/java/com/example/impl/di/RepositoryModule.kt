package com.example.impl.di

import com.example.api.FireStoreRepository
import com.example.impl.repository.FireStoreRepositoryImpl
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