package com.example.webrtc.impl

import com.example.event.EventBus.eventFlow
import com.example.event.WebRtcEvent
import com.example.firestore.SignalingManager
import com.example.manager.WebRtcController
import com.example.model.Candidate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class EventController @Inject constructor(
    private val webRtcScope: CoroutineScope,
    private val webRtcController: WebRtcController,
    private val signalingManager: SignalingManager
) {
    fun start() {
        webRtcScope.launch {
            eventFlow.collect { event ->
                when (event) {
                    is WebRtcEvent.Host -> handleHostEvent(event)
                    is WebRtcEvent.Guest -> handleGuestEvent(event)
                }
            }
        }
    }

    private fun handleGuestEvent(event: WebRtcEvent.Guest) {
        when (event) {
            is WebRtcEvent.Guest.ReceiveOffer -> {
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Guest.SendAnswer -> {
                webRtcController.createAnswer(event.roomId)
            }

            is WebRtcEvent.Guest.SendIceToHost -> {
                signalingManager.sendIceCandidateToRoom(
                    candidate = event.ice,
                    type = Candidate.ANSWER,
                    roomId = event.roomId
                )
            }

            is WebRtcEvent.Guest.SendSdpToHost -> {
                signalingManager.sendSdpToRoom(
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

    private fun handleHostEvent(event: WebRtcEvent.Host) {
        when (event) {
            is WebRtcEvent.Host.SendOffer -> {
                webRtcController.createOffer(event.roomId)
            }

            is WebRtcEvent.Host.ReceiveAnswer -> {
                webRtcController.setRemoteDescription(event.sdp)
            }

            is WebRtcEvent.Host.SendIceToGuest -> {
                signalingManager.sendIceCandidateToRoom(
                    candidate = event.ice,
                    type = Candidate.OFFER,
                    roomId = event.roomId
                )
            }

            is WebRtcEvent.Host.SendSdpToGuest -> {
                signalingManager.sendSdpToRoom(
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