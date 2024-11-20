package com.example.call

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.call.navigation.roomIdArg
import com.example.call.state.CallState
import com.example.call.ui.ChatMessage
import com.example.webrtc.client.api.WebRtcClient
import com.example.webrtc.client.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
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

    init {
        viewModelScope.launch {
            webRtcClient.getMessages()
                .filter { it is Message.PlainString }
                .map {
                    ChatMessage(
                        type = ChatMessage.ChatType.OTHER,
                        message = (it as Message.PlainString).data
                    )
                }.collect { message ->
                    _uiState.update { state ->
                        if (state is CallState.Success) {
                            if (message.message == "<INPUT EVENT>") {
                                state.copy(otherUserOnInput = true)
                            } else {
                                state.copy(
                                    messages = state.messages + message
                                )
                            }
                        } else state
                    }
                }
        }
    }

    fun fetch() = viewModelScope.launch {
        val localSurface = webRtcClient.getLocalSurface()
        val remoteSurface = webRtcClient.getRemoteSurface()

        _uiState.update {
            CallState.Success(
                local = localSurface,
                remote = remoteSurface,
                messages = emptyList(),
                otherUserOnInput = false
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

    fun sendMessage(message: String) = viewModelScope.launch {
        _uiState.update { state ->
            if (state is CallState.Success) {
                state.copy(
                    messages = state.messages + ChatMessage(
                        type = ChatMessage.ChatType.ME,
                        message = message
                    )
                )
            } else state
        }
        webRtcClient.sendMessage(message)
    }

    fun onInputChange() = viewModelScope.launch {
        webRtcClient.sendMessage("<INPUT EVENT>")
    }

    fun onInputStopped() = viewModelScope.launch {
        _uiState.update { state ->
            if (state is CallState.Success) {
                state.copy(otherUserOnInput = false)
            } else state
        }
    }
}
