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

@Composable
fun WebRTCController(
    modifier: Modifier = Modifier,
    onToggleVoice: () -> Unit,
    onToggleVideo: () -> Unit,
    onDisconnect: () -> Unit
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
            onToggleVoice()
        }
        MenuButton(
            imageVector = Icons.Default.Face,
            description = "Face",
            defaultBackgroundColor = Color.Green,
            state = isVideoClicked
        ) {
            isVideoClicked.value = !isVideoClicked.value
            onToggleVideo()
        }
        MenuButton(
            imageVector = Icons.Default.Close,
            description = "Close",
            defaultBackgroundColor = Color.Red
        ) {
            onDisconnect()
        }
    }
}

@Composable
@Preview
fun ControllerPreview() {

}