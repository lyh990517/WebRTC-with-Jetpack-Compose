package com.example.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webrtc.sdk.factory.WebRtcClientFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    var rooms by mutableStateOf<List<String>>(emptyList())
    private val webRtcClient = WebRtcClientFactory.create(getApplication())

    fun fetch() = viewModelScope.launch {
        webRtcClient.getRoomList().collect { rooms ->
            this@MainViewModel.rooms = rooms ?: emptyList()
        }
    }
}
