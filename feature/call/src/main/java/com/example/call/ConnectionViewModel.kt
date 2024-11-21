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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
            webRtcClient
                .getMessages()
                .mapToChatMessage()
                .consumeMessage()
        }
    }

    fun fetch() = viewModelScope.launch {
        _uiState.update {
            CallState.Success(
                local = webRtcClient.getLocalSurface(),
                remote = webRtcClient.getRemoteSurface(),
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
        updateState { state ->
            state.copy(messages = state.messages + message.toChatMessage())
        }
        webRtcClient.sendMessage(message)
    }

    fun sendInputEvent() = viewModelScope.launch {
        webRtcClient.sendInputEvent()
    }

    private fun updateState(update: (CallState.Success) -> CallState.Success) {
        _uiState.update { currentState ->
            if (currentState is CallState.Success) {
                update(currentState)
            } else {
                currentState
            }
        }
    }

    private suspend fun Flow<ChatMessage>.consumeMessage() =
        collect { message ->
            when (message) {
                ChatMessage.InputEvent -> {
                    _uiEffect.emit(CallEvent.InputEvent)
                }

                is ChatMessage.TextMessage -> {
                    updateState { state ->
                        val updatedMessages = state.messages.addChatMessage(message.message)

                        state.copy(messages = updatedMessages)
                    }
                }
            }
        }
}
