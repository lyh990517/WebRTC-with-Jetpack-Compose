package com.example.webrtc.client

import android.util.Log
import com.example.common.WebRtcEvent
import com.example.model.SignalType
import kotlinx.coroutines.flow.merge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EventHandler @Inject constructor(
    private val webRtcController: Controller.WebRtc,
    private val signaling: Signaling
) {
    suspend fun start() {
        merge(
            webRtcController.getEvent(),
            signaling.getEvent()
        ).collect { event ->
            when (event) {
                is WebRtcEvent.Host -> handleHostEvent(event)
                is WebRtcEvent.Guest -> handleGuestEvent(event)
            }
        }
    }

    private suspend fun handleGuestEvent(event: WebRtcEvent.Guest) {
        when (event) {
            is WebRtcEvent.Guest.ReceiveOffer -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host ----> Guest: Received Offer -> Setting remote SDP [type=${event.sdp.type}]"
                )
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Guest.SendAnswer -> {
                Log.i("WebRTC EventHandler", "Guest ----> Host: Sending Answer -> Creating answer SDP")
                webRtcController.createAnswer()
            }

            is WebRtcEvent.Guest.SetLocalSdp -> {
                Log.i("WebRTC EventHandler", "Guest: Setting Local SDP [type=${event.sdp.type}]")
                webRtcController.setLocalDescription(event.sdp, event.observer)
            }

            is WebRtcEvent.Guest.SendIceToHost -> {
                Log.i("WebRTC EventHandler", "Guest ----> Host: Sending ICE Candidate [candidate=${event.ice}]")
                signaling.sendIce(
                    ice = event.ice,
                    type = SignalType.ANSWER,
                )
            }

            is WebRtcEvent.Guest.SendSdpToHost -> {
                Log.i("WebRTC EventHandler", "Guest ----> Host: Sending SDP [type=${event.sdp.type}]")
                signaling.sendSdp(
                    sdp = event.sdp
                )
            }

            is WebRtcEvent.Guest.SetRemoteIce -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host ----> Guest: Received ICE Candidate -> Adding to remote [candidate=${event.ice}]"
                )
                webRtcController.addIceCandidate(event.ice)
            }
        }
    }

    private suspend fun handleHostEvent(event: WebRtcEvent.Host) {
        when (event) {
            is WebRtcEvent.Host.SendOffer -> {
                Log.i("WebRTC EventHandler", "Host ----> Guest: Sending Offer -> Creating offer SDP")
                webRtcController.createOffer()
            }

            is WebRtcEvent.Host.SetLocalSdp -> {
                Log.i("WebRTC EventHandler", "Host: Setting Local SDP [type=${event.sdp.type}]")
                webRtcController.setLocalDescription(event.sdp, event.observer)
            }

            is WebRtcEvent.Host.ReceiveAnswer -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Guest ----> Host: Received Answer SDP [type=${event.sdp.type}] -> Setting remote SDP"
                )
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Host.SendIceToGuest -> {
                Log.i("WebRTC EventHandler", "Host ----> Guest: Sending ICE Candidate [candidate=${event.ice}]")
                signaling.sendIce(
                    ice = event.ice,
                    type = SignalType.OFFER,
                )
            }

            is WebRtcEvent.Host.SendSdpToGuest -> {
                Log.i("WebRTC EventHandler", "Host ----> Guest: Sending SDP [type=${event.sdp.type}]")
                signaling.sendSdp(
                    sdp = event.sdp
                )
            }

            is WebRtcEvent.Host.SetRemoteIce -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Guest ----> Host: Received ICE Candidate -> Adding to remote [candidate=${event.ice}]"
                )
                webRtcController.addIceCandidate(event.ice)
            }
        }
    }
}