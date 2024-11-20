package com.example.home

import androidx.compose.runtime.mutableStateListOf
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
    val rooms = mutableStateListOf<String>()

    fun fetch() = viewModelScope.launch {
        webRtcClient.getRoomList().forEach {
            rooms.add(it)
        }
    }
}