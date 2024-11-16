package com.example.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.circuit.ConnectScreen
import com.example.circuit.HomeScreen
import com.example.model.RoomStatus
import com.example.webrtc.api.WebRtcClient
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val webRtcClient: WebRtcClient
) : Presenter<HomeUiState> {

    @Composable
    override fun present(): HomeUiState {
        var roomId by remember { mutableStateOf("") }

        return HomeUiState(
            roomId = roomId,
            eventSink = { event ->
                when (event) {
                    is HomeEvent.Input -> {
                        roomId = event.roomId
                    }

                    is HomeEvent.Connect -> {
                        val status = webRtcClient.getRoomStatus(roomId)

                        when (status) {
                            RoomStatus.NEW -> {
                                navigator.goTo(ConnectScreen(roomId, event.isHost))
                            }

                            RoomStatus.TERMINATED -> {
                                // nothing
                            }
                        }
                    }
                }
            }
        )
    }

    @CircuitInject(HomeScreen::class, ActivityRetainedComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(
            navigator: Navigator,
            webRtcClient: WebRtcClient
        ): HomePresenter
    }
}