package com.example.webrtc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webrtc.client.SignalingClient
import com.example.webrtc.client.WebRTCClient
import com.example.webrtc.event.WebRTCEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val signalingClient: SignalingClient,
    private val webRTCClient: WebRTCClient
) : ViewModel() {
    val signalEvent = signalingClient.eventFlow
    val peerConnectionEvent = webRTCClient.eventFlow
    private val _webRTCEvent = MutableSharedFlow<WebRTCEvent>()
    val webRTCEvent = _webRTCEvent
    fun initSignal(roomId: String) = viewModelScope.launch {
        signalingClient.initialize(roomId)
    }

    fun initRTC() = viewModelScope.launch {
        _webRTCEvent.emit(WebRTCEvent.Initialize(webRTCClient))
    }

    fun connect() = viewModelScope.launch {
        signalingClient.connect()
    }

    fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean, roomId: String) =
        viewModelScope.launch {
            webRTCClient.sendIceCandidate(candidate, isJoin, roomId)
        }

    fun addCandidate(candidate: IceCandidate?) = viewModelScope.launch {
        webRTCClient.addCandidate(candidate)
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) =
        viewModelScope.launch {
            webRTCClient.onRemoteSessionReceived(sessionDescription)
        }

    fun answer(roomId: String) = viewModelScope.launch {
        webRTCClient.answer(roomId)
    }
}