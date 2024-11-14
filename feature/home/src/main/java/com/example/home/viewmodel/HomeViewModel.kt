package com.example.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.home.HomeSideEffect
import com.example.webrtc.api.FireStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fireStoreRepository: FireStoreRepository
) : ViewModel() {
    val effect = MutableSharedFlow<HomeSideEffect>()

    fun connect(roomId: String, isHost: Boolean) = viewModelScope.launch {
        fireStoreRepository.getRoomInfo(roomId).collect { snapshot ->
            if (snapshot["type"] == "END_CALL") {
                effect.emit(HomeSideEffect.RoomAlreadyEnded)
            } else {
                effect.emit(HomeSideEffect.EnterRoom(roomId, isHost))
            }
        }
    }
}
