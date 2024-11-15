package com.example.connect

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.circuit.wrapEventSink
import com.example.interactor.WebRTCInteractor
import com.example.interactor.WebRTCState
import com.example.screen.ConnectScreen
import com.example.screen.ConnectType
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import org.webrtc.SurfaceViewRenderer

class ConnectPresenter @AssistedInject constructor(
    @Assisted private val screen: ConnectScreen,
    @Assisted private val navigator: Navigator,
    @ApplicationContext private val context: Context,
    private val webRTCInteractor: WebRTCInteractor
) : Presenter<ConnectUiState> {

    @Composable
    override fun present(): ConnectUiState {
        val state by webRTCInteractor.webRTCStateFlow.collectAsState()

        DisposableEffect(screen.accessCode) {
            webRTCInteractor.load(
                WebRTCState(
                    localSurfaceView = SurfaceViewRenderer(context),
                    remoteSurfaceView = SurfaceViewRenderer(context),
                    accessCode = screen.accessCode,
                    isJoin = ConnectType.isJoin(screen.type)
                )
            )
            onDispose {
                webRTCInteractor.close()
            }
        }
        val eventSink: CoroutineScope.(ConnectUiEvent) -> Unit = { event -> }

        return ConnectUiState(
            state.localSurfaceView,
            state.remoteSurfaceView,
            wrapEventSink(eventSink)
        )
    }

    @CircuitInject(ConnectScreen::class, ActivityRetainedComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(
            screen: ConnectScreen,
            navigator: Navigator
        ): ConnectPresenter
    }
}