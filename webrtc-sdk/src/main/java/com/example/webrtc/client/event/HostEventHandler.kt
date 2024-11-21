package com.example.webrtc.client.event

import android.util.Log
import com.example.webrtc.client.controller.Controller
import com.example.webrtc.client.signaling.SignalType
import com.example.webrtc.client.signaling.Signaling
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HostEventHandler @Inject constructor(
    private val webRtcController: Controller.WebRtc,
    private val signaling: Signaling
) {
    suspend fun handleHostEvent(event: WebRtcEvent.Host) {
        when (event) {
            is WebRtcEvent.Host.SendOffer -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host(You) ---> Guest: Sending Offer -> Creating offer SDP"
                )
                webRtcController.createOffer()
            }

            is WebRtcEvent.Host.SetLocalSdp -> {
                Log.i(
                    "WebRTC EventHandler",
                    "Host(You): Setting Local SDP [type=${event.sdp.type}]"
                )
                webRtcController.setLocalDescription(event.sdp, event.observer)
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
        }
    }
}