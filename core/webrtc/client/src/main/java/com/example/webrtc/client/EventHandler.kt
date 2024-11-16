package com.example.webrtc.client

import com.example.common.WebRtcEvent
import com.example.model.CandidateType
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
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Guest.SendAnswer -> {
                webRtcController.createAnswer(event.roomId)
            }

            is WebRtcEvent.Guest.SendIceToHost -> {
                signaling.sendIce(
                    ice = event.ice,
                    type = CandidateType.ANSWER,
                    roomId = event.roomId
                )
            }

            is WebRtcEvent.Guest.SendSdpToHost -> {
                signaling.sendSdp(
                    sdp = event.sdp,
                    roomId = event.roomId
                )
            }

            is WebRtcEvent.Guest.SetLocalIce -> {
                webRtcController.addIceCandidate(event.ice)
            }

            is WebRtcEvent.Guest.SetLocalSdp -> {
                webRtcController.setLocalDescription(event.sdp, event.observer)
            }

            is WebRtcEvent.Guest.SetRemoteIce -> {
                webRtcController.addIceCandidate(event.ice)
            }
        }
    }

    private suspend fun handleHostEvent(event: WebRtcEvent.Host) {
        when (event) {
            is WebRtcEvent.Host.SendOffer -> {
                webRtcController.createOffer(event.roomId)
            }

            is WebRtcEvent.Host.ReceiveAnswer -> {
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Host.SendIceToGuest -> {
                signaling.sendIce(
                    ice = event.ice,
                    type = CandidateType.OFFER,
                    roomId = event.roomId
                )
            }

            is WebRtcEvent.Host.SendSdpToGuest -> {
                signaling.sendSdp(
                    sdp = event.sdp,
                    roomId = event.roomId
                )
            }

            is WebRtcEvent.Host.SetLocalIce -> {
                webRtcController.addIceCandidate(event.ice)
            }

            is WebRtcEvent.Host.SetLocalSdp -> {
                webRtcController.setLocalDescription(event.sdp, event.observer)
            }

            is WebRtcEvent.Host.SetRemoteIce -> {
                webRtcController.addIceCandidate(event.ice)
            }
        }
    }
}