package com.example.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.home.HomeSideEffect
import com.example.webrtc.api.WebRtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val webRtcClient: WebRtcClient
) : ViewModel() {
    val effect = MutableSharedFlow<HomeSideEffect>()

    fun connect(roomId: String, isHost: Boolean) = viewModelScope.launch {
        webRtcClient.getRoomInformation(roomId).collect { snapshot ->
            if (snapshot["type"] == "END_CALL") {
                effect.emit(HomeSideEffect.RoomAlreadyEnded)
            } else {
                effect.emit(HomeSideEffect.EnterRoom(roomId, isHost))
            }
        }
    }
}
