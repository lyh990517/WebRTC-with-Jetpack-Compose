package com.example.webrtc.client.event

import android.util.Log
import com.example.webrtc.client.controller.Controller
import com.example.webrtc.client.signaling.SignalType
import com.example.webrtc.client.signaling.Signaling
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GuestEventHandler @Inject constructor(
    private val webRtcController: Controller.WebRtc,
    private val signaling: Signaling
) {
    suspend fun handleGuestEvent(event: WebRtcEvent.Guest) {
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

            is WebRtcEvent.Guest.SetLocalSdp -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Guest(You): Setting Local SDP [type=${event.sdp.type}]"
                )
                webRtcController.setLocalDescription(event.sdp, event.observer)
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
        }
    }
}