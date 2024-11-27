package com.example.call

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.call.ui.ControllerUi
import com.example.circuit.ConnectScreen
import com.example.circuit.HomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.ui.ui
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer

const val connectionRoute = "connection"
const val roomIdArg = "roomId"
const val isHostArg = "isHost"

@Composable
@CircuitInject(ConnectScreen::class, ActivityRetainedComponent::class)
fun ConnectionScreen(
    uiState: ConnectionUiState,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(200)
        uiState.eventSink(ConnectionEvent.Connect)
    }

    CallContent(
        local = uiState.local,
        remote = uiState.remote,
        onEvent = uiState.eventSink
    )
}

@Composable
private fun CallContent(
    local: SurfaceViewRenderer,
    remote: SurfaceViewRenderer,
    onEvent: (ConnectionEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { remote }
        )
        AndroidView(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(0.5f)
                .align(Alignment.BottomStart),
            factory = { local }
        )
        ControllerUi(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            onEvent = onEvent
        )
    }
}