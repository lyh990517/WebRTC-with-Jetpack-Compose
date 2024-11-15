package com.example.connect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.WebRTCView
import com.example.screen.ConnectScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent


@CircuitInject(ConnectScreen::class, ActivityRetainedComponent::class)
@Composable
internal fun ConnectScreen(
    state: ConnectUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        state.remoteSurfaceView?.let { view ->
            WebRTCView(
                modifier = Modifier.fillMaxSize(),
                view
            )
        }
        state.localSurfaceView?.let { view ->
            WebRTCView(
                modifier = Modifier
                    .width(150.dp)
                    .height(200.dp)
                    .padding(25.dp)
                    .align(Alignment.BottomStart),
                view
            )
        }
    }
}