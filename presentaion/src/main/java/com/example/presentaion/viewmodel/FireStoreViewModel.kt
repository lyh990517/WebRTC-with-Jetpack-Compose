package com.example.presentaion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.state.FireStoreState
import com.example.usecase.GetRoomInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FireStoreViewModel @Inject constructor(
    private val getRoomInfoUseCase: GetRoomInfoUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<FireStoreState>(FireStoreState.Idle)
    val state = _state.asStateFlow()
    fun getRoomInfo(roomId: String, isJoin: Boolean) = viewModelScope.launch {
        val a = getRoomInfoUseCase(roomId).first()
        _state.emit(FireStoreState.EnterRoom(isJoin))
    }
}