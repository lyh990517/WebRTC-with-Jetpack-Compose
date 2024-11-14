package com.example.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.FireStoreRepository
import com.example.api.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fireStoreRepository: FireStoreRepository
) : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)
    val state = _state.asStateFlow()
    fun getRoomInfo(roomId: String, isHost: Boolean) = viewModelScope.launch {
        fireStoreRepository.getRoomInfo(roomId).collect { snapshot ->
            if (snapshot["type"] == "END_CALL") {
                _state.emit(HomeState.RoomAlreadyEnded)
            } else {
                _state.emit(HomeState.EnterRoom(roomId, isHost))
            }
        }
    }
}