package com.example.webrtc.impl

import com.example.webrtc.impl.manager.FireStoreManager
import com.example.webrtc.impl.manager.PeerConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
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
                    is GuestEvent.SetRemoteSdp -> {
                        peerConnectionManager.setRemoteDescription(it.sdp)
                    }

                    is GuestEvent.SendAnswer -> {
                        peerConnectionManager.createAnswer(it.roomId)
                    }

                    is GuestEvent.SendIceToHost -> {
                        fireStoreManager.sendAnswerIceCandidateToRoom(it.ice, it.roomId)
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