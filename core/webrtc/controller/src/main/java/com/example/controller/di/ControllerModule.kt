package com.example.controller.di

import com.example.controller.LocalResourceController
import com.example.controller.WebRtcController
import com.example.webrtc.client.Controller
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