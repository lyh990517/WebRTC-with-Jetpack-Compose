package com.example.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.webrtc.sdk.factory.WebRtcClientFactory
import com.example.home.ConnectButton
import com.example.home.FooterText
import com.example.home.HomeNavResult
import com.example.home.Logo
import com.example.home.MediaProjectionConnectButton
import com.example.home.RoomDialog
import com.example.home.RoomListButton

@Composable
fun HomeScreen(
    onNavResult: (HomeNavResult) -> Unit
) {
    val context = LocalContext.current
    val webRtcClient = remember { WebRtcClientFactory.create(context) }
    var roomId by remember { mutableStateOf("") }
    var isDialogVisible by remember { mutableStateOf(false) }
    var rooms by remember { mutableStateOf<List<String>?>(emptyList()) }

    LaunchedEffect(Unit) {
        webRtcClient.getRoomList().collect { roomList ->
            rooms = roomList
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
            onNavResult(HomeNavResult.ToDefault(roomId))
        } else {
            Toast.makeText(context, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo()

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = roomId,
            onValueChange = { it: String -> roomId = it },
            label = { Text(text = "Room Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp)
                .padding(horizontal = 32.dp)
        )

        ConnectButton(
            context = context,
            onNavResult = onNavResult,
            roomId = roomId,
            permissionsLauncher = permissionsLauncher
        )

        Spacer(modifier = Modifier.height(16.dp))

        MediaProjectionConnectButton(
            context = context,
            onNavResult = onNavResult,
            roomId = roomId,
            permissionsLauncher = permissionsLauncher
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoomListButton { isDialogVisible = true }

        Spacer(modifier = Modifier.weight(1f))

        FooterText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }

    if (isDialogVisible) {
        RoomDialog(
            rooms = rooms ?: emptyList(),
            onDismiss = { isDialogVisible = false },
            onRoomClick = { room ->
                isDialogVisible = false
                onNavResult(HomeNavResult.ToDefault(room))
            }
        )
    }
}

fun checkCameraAndAudioPermission(
    context: Context,
    goToCall: () -> Unit,
    permissionsLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
) {
    val cameraGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    val audioGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    if (!cameraGranted || !audioGranted) {
        permissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    } else {
        goToCall()
    }
}
