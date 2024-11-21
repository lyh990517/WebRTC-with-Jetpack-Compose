package com.example.call

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.call.navigation.roomIdArg
import com.example.call.state.CallEvent
import com.example.call.state.CallState
import com.example.call.ui.chat.ChatMessage
import com.example.webrtc.client.api.WebRtcClient
import com.example.webrtc.client.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
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

    private val _uiEffect = MutableSharedFlow<CallEvent>()
    val uiEffect = _uiEffect.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    private val roomId = savedStateHandle.get<String>(roomIdArg) ?: ""

    init {
        viewModelScope.launch {
            webRtcClient.getMessages()
                .map { message ->
                    when (message) {
                        Message.InputEvent -> ChatMessage.InputEvent
                        is Message.PlainString -> {
                            ChatMessage.TextMessage(
                                type = ChatMessage.ChatType.OTHER,
                                message = message.data
                            )
                        }
                    }
                }.collect { message ->
                    when (message) {
                        ChatMessage.InputEvent -> {
                            _uiEffect.emit(CallEvent.InputEvent)
                        }

                        is ChatMessage.TextMessage -> {
                            _uiState.update {
                                val state = it as CallState.Success

                                state.copy(
                                    messages = state.messages + message
                                )
                            }
                        }
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
                messages = emptyList()
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
        _uiState.update {
            val state = it as CallState.Success

            state.copy(
                messages = state.messages + ChatMessage.TextMessage(
                    type = ChatMessage.ChatType.ME,
                    message = message
                )
            )
        }
        webRtcClient.sendMessage(message)
    }

    fun onInputChange() = viewModelScope.launch {
        webRtcClient.sendInputEvent()
    }
}
