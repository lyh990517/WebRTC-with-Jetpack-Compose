package com.example.data.di

import com.example.data.repository.FireStoreRepositoryImpl
import com.example.domain.repository.FireStoreRepository
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