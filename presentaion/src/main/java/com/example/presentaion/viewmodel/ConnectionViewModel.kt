package com.example.presentaion.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.client.WebRtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    application: Application,
    private val webRTCClient: WebRtcClient,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val roomId = savedStateHandle["roomID"] ?: ""
    private val isHost = savedStateHandle["isHost"] ?: false

    fun connect() = viewModelScope.launch {
        webRTCClient.connect(roomId, isHost)
    }

    fun toggleVoice() = viewModelScope.launch {
        webRTCClient.toggleVoice()
    }

    fun toggleVideo() = viewModelScope.launch {
        webRTCClient.toggleVideo()
    }

    fun disconnect() = viewModelScope.launch {
        try {
            webRTCClient.disconnect()
        } catch (e: Exception) {
            Log.e("error", "${e.message}")
        }
    }
}
