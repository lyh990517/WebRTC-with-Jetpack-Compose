package com.example.webrtc.sdk.event

import android.util.Log
import com.example.webrtc.sdk.controller.WebRtcController
import com.example.webrtc.sdk.signaling.SignalType
import com.example.webrtc.sdk.signaling.Signaling
import kotlinx.coroutines.flow.merge

internal class EventHandler(
    private val webRtcController: WebRtcController,
    private val signaling: Signaling,
) {
    private val events = merge(
        webRtcController.getEvent(),
        signaling.getEvent(),
    )

    suspend fun start() {
        events.collect { event ->
            when (event) {
                is WebRtcEvent.Host -> handleHostEvent(event)
                is WebRtcEvent.Guest -> handleGuestEvent(event)
                is WebRtcEvent.StateChange -> handleStateChangeEvent(event)
                WebRtcEvent.None -> {}
            }
        }
    }

    fun getEvents() = events

    private fun handleStateChangeEvent(event: WebRtcEvent.StateChange) {
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

    private suspend fun handleGuestEvent(event: WebRtcEvent.Guest) {
        when (event) {
            is WebRtcEvent.Guest.ReceiveOffer -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host ---> Guest(You): Sending Offer -> Setting remote SDP [type=${event.sdp.type}]"
                )
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Guest.SendAnswer -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Guest(You) ---> Host: Sending Answer -> Creating answer SDP"
                )
                webRtcController.createAnswer()
            }

            is WebRtcEvent.Guest.SendIceToHost -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Guest(You) ---> Host: Sending ICE Candidate [candidate=${event.ice}]"
                )
                signaling.sendIce(
                    ice = event.ice,
                    type = SignalType.ANSWER,
                )
            }

            is WebRtcEvent.Guest.SendSdpToHost -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Guest(You) ---> Host: Sending SDP [type=${event.sdp.type}]"
                )
                signaling.sendSdp(
                    sdp = event.sdp
                )
            }

            is WebRtcEvent.Guest.SetRemoteIce -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host ---> Guest(You): Sending ICE Candidate -> Adding to remote [candidate=${event.ice}]"
                )
                webRtcController.addIceCandidate(event.ice)
            }

            is WebRtcEvent.Guest.VideoTrackReceived -> {
                webRtcController.sinkVideoTrack(event.track)
            }
        }
    }

    private suspend fun handleHostEvent(event: WebRtcEvent.Host) {
        when (event) {
            is WebRtcEvent.Host.SendOffer -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host(You) ---> Guest: Sending Offer -> Creating offer SDP"
                )
                webRtcController.createOffer()
            }

            is WebRtcEvent.Host.ReceiveAnswer -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Guest ---> Host(You): Sending Answer SDP [type=${event.sdp.type}] -> Setting remote SDP"
                )
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Host.SendIceToGuest -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host(You) ---> Guest: Sending ICE Candidate [candidate=${event.ice}]"
                )
                signaling.sendIce(
                    ice = event.ice,
                    type = SignalType.OFFER,
                )
            }

            is WebRtcEvent.Host.SendSdpToGuest -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host(You) ---> Guest: Sending SDP [type=${event.sdp.type}]"
                )
                signaling.sendSdp(
                    sdp = event.sdp
                )
            }

            is WebRtcEvent.Host.SetRemoteIce -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Guest ---> Host(You): Sending ICE Candidate -> Adding to remote [candidate=${event.ice}]"
                )
                webRtcController.addIceCandidate(event.ice)
            }

            is WebRtcEvent.Host.VideoTrackReceived -> {
                webRtcController.sinkVideoTrack(event.track)
            }
        }
    }
}
