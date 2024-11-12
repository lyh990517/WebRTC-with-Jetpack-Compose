package com.example.presentaion.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.client.WebRTCClient
import com.example.domain.event.SignalEvent
import com.example.domain.event.WebRTCEvent
import com.example.domain.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val webRTCClient: WebRTCClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId = MutableStateFlow(savedStateHandle["roomID"] ?: "")
    private val isJoin = MutableStateFlow(savedStateHandle["isJoin"] ?: false)

    private val _webRTCEvent = MutableSharedFlow<WebRTCEvent>()
    val webRTCEvent = _webRTCEvent

    val peerConnectionEvent = webRTCClient.getEvent()

    val uiState = MutableStateFlow<UiState>(UiState.UnInitialized)

    init {
        viewModelScope.launch {
            initSignal()
        }
    }

    private fun initSignal() = viewModelScope.launch {
        webRTCClient.initialize(roomId.value)
    }

    fun initRTC() = viewModelScope.launch {
        _webRTCEvent.emit(WebRTCEvent.Initialize(webRTCClient))
    }

    fun connect() = viewModelScope.launch {
        webRTCClient.connect(roomId.value)
    }

    fun sendIceCandidate(candidate: IceCandidate?) =
        viewModelScope.launch {
            webRTCClient.sendIceCandidate(candidate, isJoin.value, roomId.value)
        }

    fun addCandidate(candidate: IceCandidate?) = viewModelScope.launch {
        webRTCClient.addCandidate(candidate)
    }

    fun call() = viewModelScope.launch {
        if (!isJoin.value) webRTCClient.call(roomId.value)
    }

    fun toggleVoice() = viewModelScope.launch {
        webRTCClient.toggleVoice()
    }

    fun toggleVideo() = viewModelScope.launch {
        webRTCClient.toggleVideo()
    }

    fun closeSession() = viewModelScope.launch {
        try {
            webRTCClient.closeSession()
            _webRTCEvent.emit(WebRTCEvent.CloseSession)
        } catch (e: Exception) {
            Log.e("error", "${e.message}")
        }
    }
}