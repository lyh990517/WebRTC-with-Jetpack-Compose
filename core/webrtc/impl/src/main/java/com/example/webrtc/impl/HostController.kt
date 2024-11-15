package com.example.webrtc.impl

import com.example.manager.FireStoreManager
import com.example.manager.HostEvent
import com.example.manager.PeerConnectionManager
import com.example.manager.hostEvent
import com.example.model.Candidate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class HostController @Inject constructor(
    private val peerConnectionManager: PeerConnectionManager,
    private val fireStoreManager: FireStoreManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        scope.launch {
            hostEvent.collect {
                when (it) {
                    is HostEvent.SendOffer -> {
                        peerConnectionManager.createOffer(it.roomId)
                    }

                    is HostEvent.ReceiveAnswer -> {
                        peerConnectionManager.setRemoteDescription(it.sdp)
                    }

                    is HostEvent.SendIceToGuest -> {
                        fireStoreManager.sendIceCandidateToRoom(
                            candidate = it.ice,
                            type = Candidate.OFFER,
                            roomId = it.roomId
                        )
                    }

                    is HostEvent.SendSdpToGuest -> {
                        fireStoreManager.sendSdpToRoom(sdp = it.sdp, roomId = it.roomId)
                    }

                    is HostEvent.SetLocalIce -> {
                        peerConnectionManager.addIceCandidate(it.ice)
                    }

                    is HostEvent.SetLocalSdp -> {
                        peerConnectionManager.setLocalDescription(it.sdp, it.observer)
                    }

                    is HostEvent.SetRemoteIce -> {
                        peerConnectionManager.addIceCandidate(it.ice)
                    }
                }
            }
        }
    }
}