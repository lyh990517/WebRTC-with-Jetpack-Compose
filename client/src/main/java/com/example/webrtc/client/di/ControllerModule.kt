package com.example.webrtc.client.di

import com.example.webrtc.client.controller.LocalResourceController
import com.example.webrtc.client.controller.WebRtcController
import com.example.webrtc.client.controller.Controller
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