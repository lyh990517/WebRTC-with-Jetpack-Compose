package com.example.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webrtc.client.api.WebRtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val webRtcClient: WebRtcClient
) : ViewModel() {
    var rooms by mutableStateOf<List<String>>(emptyList())

    fun fetch() = viewModelScope.launch {
        webRtcClient.getRoomList().collect { rooms ->
            this@MainViewModel.rooms = rooms ?: emptyList()
        }
    }
}