package com.example.webrtc.client.event

import android.util.Log
import com.example.webrtc.client.signaling.PeerStatus
import com.example.webrtc.client.signaling.Signaling
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class StatusEventHandler @Inject constructor(
    private val signaling: Signaling
) {
    private var lastSignalingStatus = ""
    private var lastIceConnectionStatus = ""

    fun handleStateChangeEvent(event: WebRtcEvent.StateChange) {
        when (event) {
            is WebRtcEvent.StateChange.Connection -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Connection State Changes : ${event.state?.name}"
                )
            }

            is WebRtcEvent.StateChange.IceConnection -> {
                Log.i(
                    "WebRTC EventHandler",
                    "IceConnection State Changes : ${event.state?.name}"
                )
                lastIceConnectionStatus = event.state?.name ?: ""

                sendMyStatus()
            }

            is WebRtcEvent.StateChange.IceGathering -> {
                Log.i(
                    "WebRTC EventHandler",
                    "IceGathering State Changes : ${event.state?.name}"
                )
            }

            is WebRtcEvent.StateChange.Signaling -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Signaling State Changes : ${event.state?.name}"
                )
                lastSignalingStatus = event.state?.name ?: ""

                sendMyStatus()
            }

            is WebRtcEvent.StateChange.Buffer -> {
                Log.i(
                    "WebRTC EventHandler",
                    "DataChannel OnBufferAmountChanged : ${event.amount}"
                )
            }

            is WebRtcEvent.StateChange.DataChannel -> {
                Log.i(
                    "WebRTC EventHandler",
                    "DataChannel State Changes : ${event.state.name}"
                )
            }
        }
    }

    private fun sendMyStatus() {
        signaling.sendMyStatus(
            PeerStatus(
                iceConnection = lastIceConnectionStatus,
                signaling = lastSignalingStatus
            )
        )
    }
}