package com.example.webrtc.impl

import com.example.manager.FireStoreManager
import com.example.manager.GuestEvent
import com.example.manager.PeerConnectionManager
import com.example.manager.guestEvent
import com.example.model.Candidate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class GuestController @Inject constructor(
    private val peerConnectionManager: PeerConnectionManager,
    private val fireStoreManager: FireStoreManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        scope.launch {
            guestEvent.collect {
                when (it) {
                    is GuestEvent.ReceiveOffer -> {
                        peerConnectionManager.setRemoteDescription(it.sdp)
                    }

                    is GuestEvent.SendAnswer -> {
                        peerConnectionManager.createAnswer(it.roomId)
                    }

                    is GuestEvent.SendIceToHost -> {
                        fireStoreManager.sendIceCandidateToRoom(
                            candidate = it.ice,
                            type = Candidate.ANSWER,
                            roomId = it.roomId
                        )
                    }

                    is GuestEvent.SendSdpToHost -> {
                        fireStoreManager.sendSdpToRoom(sdp = it.sdp, roomId = it.roomId)
                    }

                    is GuestEvent.SetLocalIce -> {
                        peerConnectionManager.addIceCandidate(it.ice)
                    }

                    is GuestEvent.SetLocalSdp -> {
                        peerConnectionManager.setLocalDescription(it.sdp, it.observer)
                    }

                    is GuestEvent.SetRemoteIce -> {
                        peerConnectionManager.addIceCandidate(it.ice)
                    }
                }
            }
        }
    }
}