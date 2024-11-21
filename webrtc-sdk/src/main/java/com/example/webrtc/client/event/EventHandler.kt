package com.example.webrtc.client.event

import android.util.Log
import com.example.webrtc.client.controller.Controller
import com.example.webrtc.client.signaling.PeerStatus
import com.example.webrtc.client.signaling.SignalType
import com.example.webrtc.client.signaling.Signaling
import kotlinx.coroutines.flow.merge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EventHandler @Inject constructor(
    private val webRtcController: Controller.WebRtc,
    private val signaling: Signaling,
    private val hostEventHandler: HostEventHandler,
    private val guestEventHandler: GuestEventHandler,
    private val statusEventHandler: StatusEventHandler
) {
    suspend fun start() {
        merge(
            webRtcController.getEvent(),
            signaling.getEvent()
        ).collect { event ->
            when (event) {
                is WebRtcEvent.Host -> hostEventHandler.handleHostEvent(event)
                is WebRtcEvent.Guest -> guestEventHandler.handleGuestEvent(event)
                is WebRtcEvent.StateChange -> statusEventHandler.handleStateChangeEvent(event)
            }
        }
    }
}