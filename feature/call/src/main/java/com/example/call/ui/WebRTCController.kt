package com.example.call.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.call.ConnectionEvent

@Composable
fun ControllerUi(
    modifier: Modifier = Modifier,
    onEvent: (ConnectionEvent) -> Unit
) {
    val isCallClicked = rememberSaveable { mutableStateOf(false) }
    val isVideoClicked = rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MenuButton(
            imageVector = Icons.Default.Call,
            description = "Call",
            defaultBackgroundColor = Color.Green,
            state = isCallClicked
        ) {
            isCallClicked.value = !isCallClicked.value
            onEvent(ConnectionEvent.ToggleVoice)
        }
        MenuButton(
            imageVector = Icons.Default.Face,
            description = "Face",
            defaultBackgroundColor = Color.Green,
            state = isVideoClicked
        ) {
            isVideoClicked.value = !isVideoClicked.value
            onEvent(ConnectionEvent.ToggleVideo)
        }
        MenuButton(
            imageVector = Icons.Default.Close,
            description = "Close",
            defaultBackgroundColor = Color.Red
        ) {
            onEvent(ConnectionEvent.DisConnect)
        }
    }
}

@Composable
@Preview
fun ControllerPreview() {

}