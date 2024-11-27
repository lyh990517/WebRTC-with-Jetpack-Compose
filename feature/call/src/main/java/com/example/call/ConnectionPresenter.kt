package com.example.call

import androidx.compose.runtime.Composable
import com.example.circuit.ConnectScreen
import com.example.webrtc.api.WebRtcClient
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent

class ConnectionPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val webRtcClient: WebRtcClient,
    private val screen: ConnectScreen
) : Presenter<ConnectionUiState> {
    @Composable
    override fun present(): ConnectionUiState {
        return ConnectionUiState(
            local = webRtcClient.getLocalSurface(),
            remote = webRtcClient.getRemoteSurface(),
            eventSink = { event ->
                when (event) {
                    is ConnectionEvent.Connect -> webRtcClient.connect(screen.roomId, screen.isHost)
                    ConnectionEvent.DisConnect -> webRtcClient.disconnect()
                    ConnectionEvent.ToggleVideo -> webRtcClient.toggleVideo()
                    ConnectionEvent.ToggleVoice -> webRtcClient.toggleVoice()
                }
            }
        )
    }

    @CircuitInject(ConnectScreen::class, ActivityRetainedComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(
            navigator: Navigator
        ): ConnectionPresenter
    }
}