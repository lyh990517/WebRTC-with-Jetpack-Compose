package com.example.data.di

import android.content.Context
import com.example.domain.LocalSurface
import com.example.domain.RemoteSurface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.webrtc.SurfaceViewRenderer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SurfaceModule {

    @Provides
    @Singleton
    @RemoteSurface
    fun providesRemoveSurface(
        @ApplicationContext context: Context
    ) = SurfaceViewRenderer(context)

    @Provides
    @Singleton
    @LocalSurface
    fun providesLocalSurface(
        @ApplicationContext context: Context
    ) = SurfaceViewRenderer(context)
}