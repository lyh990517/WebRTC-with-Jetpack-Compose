package com.example.presentaion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.state.FireStoreState
import com.example.domain.usecase.GetRoomInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FireStoreViewModel @Inject constructor(
    private val getRoomInfoUseCase: GetRoomInfoUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<FireStoreState>(FireStoreState.Idle)
    val state = _state.asStateFlow()
    fun getRoomInfo(roomId: String, isJoin: Boolean) = viewModelScope.launch {
        getRoomInfoUseCase(roomId).collect { snapshot ->
            if (snapshot["type"] == "END_CALL") {
                _state.emit(FireStoreState.RoomAlreadyEnded)
            } else {
                _state.emit(FireStoreState.EnterRoom(roomId, isJoin))
            }
        }
    }
}