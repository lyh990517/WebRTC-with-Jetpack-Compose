package com.example.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.home.ui.InputContent
import com.example.home.ui.LogoImage

@Composable
fun MainScreen(
    goToCall: (String) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    var roomId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val rooms = viewModel.rooms
    var isRoomListVisible by remember { mutableStateOf(false) } // Room 리스트 표시 여부

    LaunchedEffect(Unit) {
        viewModel.fetch()
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
            goToCall(roomId)
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
        LogoImage()
        Spacer(modifier = Modifier.height(40.dp))
        InputContent(
            roomId = { roomId },
            onInput = { roomId = it },
            onCall = {
                checkCameraAndAudioPermission(
                    context = context,
                    goToCall = { goToCall(roomId) },
                    permissionsLauncher = permissionsLauncher
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { isRoomListVisible = !isRoomListVisible }) {
            Text(if (isRoomListVisible) "Hide Rooms" else "Show Rooms")
        }

        if (isRoomListVisible) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                items(rooms) { room ->
                    Button(
                        onClick = { goToCall(room) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = room, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Created by yunho 2023",
            modifier = Modifier
                .padding(vertical = 24.dp)
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
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
