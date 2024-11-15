package com.example.manager.di

import com.example.manager.Controller
import com.example.manager.LocalResourceController
import com.example.manager.WebRtcController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ControllerModule {

    @Binds
    abstract fun bindsLocalResourceController(
        localResourceController: LocalResourceController
    ): Controller.LocalResource

    @Binds
    abstract fun bindsWebRtcController(
        webRtcController: WebRtcController
    ): Controller.WebRtc
}