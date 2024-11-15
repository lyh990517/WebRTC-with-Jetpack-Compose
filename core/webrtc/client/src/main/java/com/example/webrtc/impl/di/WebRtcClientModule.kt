package com.example.webrtc.impl.di

import com.example.model.GuestWebRtcClient
import com.example.model.HostWebRtcClient
import com.example.webrtc.api.WebRtcClient
import com.example.webrtc.impl.GuestClient
import com.example.webrtc.impl.HostClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WebRtcClientModule {

    @Binds
    @HostWebRtcClient
    abstract fun bindsHostWebRtcClient(hostClient: HostClient): WebRtcClient

    @Binds
    @GuestWebRtcClient
    abstract fun bindsGuestWebRtcClient(guestClient: GuestClient): WebRtcClient
}
