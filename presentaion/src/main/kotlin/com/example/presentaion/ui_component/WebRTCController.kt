package com.example.presentaion.ui_component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.presentaion.viewmodel.ConnectionViewModel

@Composable
fun WebRTCController(viewModel: ConnectionViewModel) {
    val isCallClicked = rememberSaveable { mutableStateOf(false) }
    val isVideoClicked = rememberSaveable { mutableStateOf(false) }
    Row(
        Modifier.fillMaxSize(),
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
            viewModel.toggleVoice()
        }
        MenuButton(
            imageVector = Icons.Default.Face,
            description = "Face",
            defaultBackgroundColor = Color.Green,
            state = isVideoClicked
        ) {
            isVideoClicked.value = !isVideoClicked.value
            viewModel.toggleVideo()
        }
        MenuButton(
            imageVector = Icons.Default.Close,
            description = "Close",
            defaultBackgroundColor = Color.Red
        ) {
            viewModel.closeSession()
        }
    }
}

@Composable
@Preview
fun ControllerPreview() {

}