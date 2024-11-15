package com.example.webrtc.impl

import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import com.example.webrtc.impl.manager.FireStoreManager
import com.example.webrtc.impl.manager.PeerConnectionManager
import com.example.webrtc.impl.manager.SignalingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WebRtcClientImpl @Inject constructor(
    private val signalingManager: SignalingManager,
    private val peerConnectionManager: PeerConnectionManager,
    private val fireStoreManager: FireStoreManager,
    private val localAudioTrack: AudioTrack,
    private val localVideoTrack: VideoTrack,
    private val videoCapturer: VideoCapturer,
    private val hostController: HostController,
    private val guestController: GuestController
) : WebRtcClient {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun connectAsHost(roomID: String) {
        scope.launch {
            hostController.start()

            peerConnectionManager.connectToPeerAsHost(roomID)

            videoCapturer.startCapture(1920, 1080, 60)

            hostEvent.emit(HostEvent.SendOffer(roomID))

            signalingManager.observeSignaling(roomID)
        }
    }

    override fun connectAsGuest(roomID: String) {
        scope.launch {
            guestController.start()

            peerConnectionManager.connectToPeerAsGuest(roomID)

            videoCapturer.startCapture(1920, 1080, 60)

            signalingManager.observeSignaling(roomID)
        }
    }

    override suspend fun getRoomStatus(roomID: String): RoomStatus =
        fireStoreManager.getRoomStatus(roomID).first()

    override fun toggleVoice() {
        localAudioTrack.setEnabled(!localAudioTrack.enabled())
    }

    override fun toggleVideo() {
        localVideoTrack.setEnabled(!localVideoTrack.enabled())
    }

    override fun disconnect() {
        localAudioTrack.dispose()
        localVideoTrack.dispose()
        peerConnectionManager.closeConnection()
        signalingManager.close()
    }
}
