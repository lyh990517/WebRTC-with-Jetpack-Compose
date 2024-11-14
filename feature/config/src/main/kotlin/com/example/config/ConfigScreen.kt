package com.example.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.icon.toPainter
import com.example.screen.ConfigScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent

@CircuitInject(ConfigScreen::class, ActivityRetainedComponent::class)
@Composable
internal fun ConfigScreen(
    state : ConfigUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.size(200.dp),
                painter = com.example.designsystem.icon.Icons.webRtc.toPainter,
                contentDescription = "Logo"
            )
            Spacer(modifier = Modifier.padding(vertical = 50.dp))

            OutlinedTextField(
                modifier = modifier,
                value = state.accessCode,
                onValueChange = { state.eventSink(ConfigUiEvent.InputAccessCode(it)) },
                label = { Text(text = "Room") }
            )
            Spacer(modifier = Modifier.padding(vertical = 20.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 40.dp),
                onClick = { state.eventSink(ConfigUiEvent.Route.CreateRoom) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "",
                    tint = Color.White
                )
                Text(text = "Create Room", color = Color.White, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.padding(vertical = 10.dp))

            Button(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                onClick = { state.eventSink(ConfigUiEvent.Route.JoinRoom) }
            ) {
                Text(text = "Join", color = Color.White, fontSize = 20.sp)
            }
        }

        Text(text = "Created by yunho 2023", Modifier.padding(bottom = 10.dp))
    }
}
