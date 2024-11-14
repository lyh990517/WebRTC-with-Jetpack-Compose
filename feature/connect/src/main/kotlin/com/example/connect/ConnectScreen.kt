package com.example.connect

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.screen.ConnectScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent


@CircuitInject(ConnectScreen::class, ActivityRetainedComponent::class)
@Composable
internal fun ConnectScreen(
    state: ConnectUiState,
    modifier: Modifier = Modifier
) {
    Column {

    }
}