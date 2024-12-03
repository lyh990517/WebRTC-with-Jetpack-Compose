package com.example.call

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ControllerButtons(
    modifier: Modifier = Modifier,
    onEvent: (ConnectionEvent) -> Unit
) {
    var isCallClicked by rememberSaveable { mutableStateOf(false) }
    var isVideoClicked by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ControllerButton(
            label = "Voice",
            isActive = isCallClicked,
            onClick = {
                isCallClicked = !isCallClicked
                onEvent(ConnectionEvent.OnToggleVoice)
            },
            icon = Icons.Default.Call
        )
        ControllerButton(
            label = "Video",
            isActive = isVideoClicked,
            onClick = {
                isVideoClicked = !isVideoClicked
                onEvent(ConnectionEvent.OnToggleVideo)
            },
            icon = Icons.Default.Face
        )
        ControllerButton(
            label = "Chat",
            isActive = false,
            onClick = { onEvent(ConnectionEvent.OnToggleChat) },
            icon = Icons.Default.Email
        )
        ControllerButton(
            label = "End",
            isActive = false,
            onClick = { onEvent(ConnectionEvent.OnDisconnect) },
            icon = Icons.Default.Close,
            activeColor = MaterialTheme.colorScheme.error,
            inactiveColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ControllerButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = if (isActive) activeColor else inactiveColor,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}


@Composable
@Preview
fun ControllerSpeechPreview() {
    ControllerButtons {

    }
}

