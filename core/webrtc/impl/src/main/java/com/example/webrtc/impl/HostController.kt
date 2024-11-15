package com.example.webrtc.impl

import com.example.webrtc.impl.manager.FireStoreManager
import com.example.webrtc.impl.manager.PeerConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
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

                    is HostEvent.SetRemoteSdp -> {
                        peerConnectionManager.setRemoteDescription(it.sdp)
                    }

                    is HostEvent.SendIceToGuest -> {
                        fireStoreManager.sendOfferIceCandidateToRoom(it.ice, it.roomId)
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