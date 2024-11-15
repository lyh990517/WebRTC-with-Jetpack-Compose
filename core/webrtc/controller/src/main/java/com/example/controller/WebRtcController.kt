package com.example.controller

import com.example.common.EventBus.eventFlow
import com.example.common.WebRtcEvent
import com.example.webrtc.client.Controller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WebRtcController @Inject constructor(
    private val webRtcScope: CoroutineScope,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val localResourceController: Controller.LocalResource,
) : Controller.WebRtc {
    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private lateinit var peerConnection: PeerConnection

    override fun connect(roomID: String, isHost: Boolean) {
        webRtcScope.launch {
            peerConnectionFactory.createPeerConnection(
                iceServer,
                createPeerConnectionHostObserver(roomID, isHost)
            )?.let { connection ->
                peerConnection = connection
            }

            val localMediaStream = localResourceController.getLocalMediaStream()

            peerConnection.addStream(localMediaStream)

            if (isHost) {
                eventFlow.emit(WebRtcEvent.Host.SendOffer(roomID))
            }
        }
    }

    override fun createOffer(roomID: String) {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                webRtcScope.launch {
                    eventFlow.emit(WebRtcEvent.Host.SetLocalSdp(observer, sdp))
                    eventFlow.emit(WebRtcEvent.Host.SendSdpToGuest(sdp = sdp, roomId = roomID))
                }
            }
        )

        peerConnection.createOffer(sdpObserver, constraints)
    }

    override fun createAnswer(roomID: String) {
        val sdpObserver = createSdpObserver(
            onSdpCreationSuccess = { sdp, observer ->
                webRtcScope.launch {
                    eventFlow.emit(WebRtcEvent.Guest.SetLocalSdp(observer, sdp))
                    eventFlow.emit(WebRtcEvent.Guest.SendSdpToHost(sdp = sdp, roomId = roomID))
                }
            }
        )

        peerConnection.createAnswer(sdpObserver, constraints)
    }

    override fun closeConnection() {
        peerConnection.close()
    }

    override fun setLocalDescription(sdp: SessionDescription, observer: SdpObserver) {
        peerConnection.setLocalDescription(observer, sdp)
    }

    override fun setRemoteDescription(sdp: SessionDescription) {
        val sdpObserver = createSdpObserver()

        peerConnection.setRemoteDescription(sdpObserver, sdp)
    }

    override fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection.addIceCandidate(iceCandidate)
    }

    private fun createSdpObserver(onSdpCreationSuccess: ((SessionDescription, SdpObserver) -> Unit)? = null) =
        object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                onSdpCreationSuccess?.let { function -> p0?.let { sdp -> function(sdp, this) } }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }

    private fun createPeerConnectionHostObserver(
        roomID: String,
        isHost: Boolean
    ): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let {
                    webRtcScope.launch {
                        if (isHost) {
                            eventFlow.emit(
                                WebRtcEvent.Host.SendIceToGuest(
                                    ice = it,
                                    roomId = roomID
                                )
                            )
                            eventFlow.emit(WebRtcEvent.Host.SetLocalIce(ice = it))
                        } else {
                            eventFlow.emit(
                                WebRtcEvent.Guest.SendIceToHost(
                                    ice = it,
                                    roomId = roomID
                                )
                            )
                            eventFlow.emit(WebRtcEvent.Guest.SetLocalIce(ice = it))
                        }
                    }
                }
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {
                p0?.let { remoteMediaStream ->
                    localResourceController.sinkToRemoteSurface(remoteMediaStream)
                }
            }

            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        }


    companion object {
        private const val ICE_SERVER_URL = "stun:stun4.l.google.com:19302"
    }
}
