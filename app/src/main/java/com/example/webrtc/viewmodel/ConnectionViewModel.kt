package com.example.webrtc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webrtc.client.SignalingClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val signalingClient: SignalingClient
) : ViewModel() {
    val eventFlow = signalingClient.eventFlow
    fun init(roomId: String) = viewModelScope.launch {
        signalingClient.initialize(roomId)
    }

    fun connect() = viewModelScope.launch(Dispatchers.IO) {
        signalingClient.connect()
    }
}