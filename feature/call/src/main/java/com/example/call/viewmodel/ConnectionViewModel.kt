package com.example.call.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.call.roomIdArg
import com.example.call.state.CallState
import com.example.webrtc.api.WebRtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    application: Application,
    private val webRtcClient: WebRtcClient,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<CallState>(CallState.Loading)
    val uiState = _uiState.stateIn(
        initialValue = CallState.Loading,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    private val roomId = savedStateHandle.get<String>(roomIdArg) ?: ""

    fun fetch() = viewModelScope.launch {
        val localSurface = webRtcClient.getLocalSurface()
        val remoteSurface = webRtcClient.getRemoteSurface()

        _uiState.update {
            CallState.Success(
                local = localSurface,
                remote = remoteSurface
            )
        }
    }

    fun connect() = viewModelScope.launch {
        webRtcClient.connect(roomId)
    }

    fun toggleVoice() = viewModelScope.launch {
        webRtcClient.toggleVoice()
    }

    fun toggleVideo() = viewModelScope.launch {
        webRtcClient.toggleVideo()
    }

    fun disconnect() = viewModelScope.launch {
        try {
            webRtcClient.disconnect()
        } catch (e: Exception) {
            Log.e("error", "${e.message}")
        }
    }
}
