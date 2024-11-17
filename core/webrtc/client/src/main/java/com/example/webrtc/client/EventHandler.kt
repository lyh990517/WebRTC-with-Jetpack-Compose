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
                Log.e(
                    "WebRTC",
                    "Guest: Received Offer -> Setting remote SDP [type=${event.sdp.type}]"
                )
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Guest.SendAnswer -> {
                Log.e("WebRTC", "Guest: Sending Answer -> Creating answer SDP")
                webRtcController.createAnswer()
            }

            is WebRtcEvent.Guest.SetLocalSdp -> {
                Log.e("WebRTC", "Guest: Setting Local SDP [type=${event.sdp.type}]")
                webRtcController.setLocalDescription(event.sdp, event.observer)
            }

            is WebRtcEvent.Guest.SendIceToHost -> {
                Log.e("WebRTC", "Guest: Sending ICE Candidate to Host [candidate=${event.ice}]")
                signaling.sendIce(
                    ice = event.ice,
                    type = SignalType.ANSWER,
                )
            }

            is WebRtcEvent.Guest.SendSdpToHost -> {
                Log.e("WebRTC", "Guest: Sending SDP to Host [type=${event.sdp.type}]")
                signaling.sendSdp(
                    sdp = event.sdp
                )
            }

            is WebRtcEvent.Guest.SetRemoteIce -> {
                Log.e(
                    "WebRTC",
                    "Guest: Received ICE Candidate -> Adding to remote [candidate=${event.ice}]"
                )
                webRtcController.addIceCandidate(event.ice)
            }
        }
    }

    private suspend fun handleHostEvent(event: WebRtcEvent.Host) {
        when (event) {
            is WebRtcEvent.Host.SendOffer -> {
                Log.e("WebRTC", "Host: Sending Offer -> Creating offer SDP")
                webRtcController.createOffer()
            }

            is WebRtcEvent.Host.SetLocalSdp -> {
                Log.e("WebRTC", "Host: Setting Local SDP [type=${event.sdp.type}]")
                webRtcController.setLocalDescription(event.sdp, event.observer)
            }

            is WebRtcEvent.Host.ReceiveAnswer -> {
                Log.e(
                    "WebRTC",
                    "Host: Received Answer SDP [type=${event.sdp.type}] -> Setting remote SDP"
                )
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Host.SendIceToGuest -> {
                Log.e("WebRTC", "Host: Sending ICE Candidate to Guest [candidate=${event.ice}]")
                signaling.sendIce(
                    ice = event.ice,
                    type = SignalType.OFFER,
                )
            }

            is WebRtcEvent.Host.SendSdpToGuest -> {
                Log.e("WebRTC", "Host: Sending SDP to Guest [type=${event.sdp.type}]")
                signaling.sendSdp(
                    sdp = event.sdp
                )
            }

            is WebRtcEvent.Host.SetRemoteIce -> {
                Log.e(
                    "WebRTC",
                    "Host: Received ICE Candidate -> Adding to remote [candidate=${event.ice}]"
                )
                webRtcController.addIceCandidate(event.ice)
            }
        }
    }
}