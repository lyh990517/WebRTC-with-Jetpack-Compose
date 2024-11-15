package com.example.call.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.call.isHostArg
import com.example.call.roomIdArg
import com.example.model.GuestWebRtcClient
import com.example.model.HostWebRtcClient
import com.example.webrtc.api.WebRtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    application: Application,
    @HostWebRtcClient private val hostClient: WebRtcClient,
    @GuestWebRtcClient private val guestClient: WebRtcClient,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val roomId = savedStateHandle.get<String>(roomIdArg) ?: ""
    private val isHost = savedStateHandle.get<Boolean>(isHostArg) ?: false

    fun connect() = viewModelScope.launch {
        if (isHost) {
            hostClient.connect(roomId)
        } else {
            guestClient.connect(roomId)
        }
    }

    fun toggleVoice() = viewModelScope.launch {
        if (isHost) {
            hostClient.toggleVoice()
        } else {
            guestClient.toggleVoice()
        }
    }

    fun toggleVideo() = viewModelScope.launch {
        if (isHost) {
            hostClient.toggleVideo()
        } else {
            guestClient.toggleVideo()
        }
    }

    fun disconnect() = viewModelScope.launch {
        try {
            if (isHost) {
                hostClient.disconnect()
            } else {
                guestClient.disconnect()
            }
        } catch (e: Exception) {
            Log.e("error", "${e.message}")
        }
    }
}
