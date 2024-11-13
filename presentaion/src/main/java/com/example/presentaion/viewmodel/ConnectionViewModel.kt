package com.example.presentaion.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.client.WebRTCClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    application: Application,
    private val webRTCClient: WebRTCClient,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val roomId = MutableStateFlow(savedStateHandle["roomID"] ?: "")
    private val isJoin = MutableStateFlow(savedStateHandle["isJoin"] ?: false)

    fun connectToRoom(
    ) = viewModelScope.launch {
        webRTCClient.connectToRoom(roomId.value)
        call()
        connect()
    }

    private fun connect() = viewModelScope.launch {
        webRTCClient.connect(roomId.value, isJoin.value)
    }

    private fun call() = viewModelScope.launch {
        if (!isJoin.value) webRTCClient.call(roomId.value)
    }

    fun toggleVoice() = viewModelScope.launch {
        webRTCClient.toggleVoice()
    }

    fun toggleVideo() = viewModelScope.launch {
        webRTCClient.toggleVideo()
    }

    fun closeSession() = viewModelScope.launch {
        try {
            webRTCClient.closeSession()
        } catch (e: Exception) {
            Log.e("error", "${e.message}")
        }
    }
}