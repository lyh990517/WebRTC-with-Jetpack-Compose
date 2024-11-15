package com.example.interactor.di

import com.example.interactor.WebRTCInteractor
import com.example.interactor.WebRTCInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class InteractorModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun provideWebRTCInteractor(webRTCInteractorImpl: WebRTCInteractorImpl): WebRTCInteractor
}