package com.example.webrtc.client.di

import android.content.Context
import android.speech.SpeechRecognizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SpeechRecognizerModule {

    @Provides
    @Singleton
    fun providesSpeechRecognizer(
        @ApplicationContext context: Context
    ): SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
}