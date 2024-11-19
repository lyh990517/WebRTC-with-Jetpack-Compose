package com.example.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.home.ui.InputContent
import com.example.home.ui.LogoImage

@Composable
fun MainScreen(
    goToCall: (String) -> Unit
) {
    var roomId by remember { mutableStateOf("") }
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
            goToCall(roomId)
        } else {
            Toast.makeText(context, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoImage()
            Spacer(modifier = Modifier.padding(vertical = 50.dp))
            InputContent(
                roomId = { roomId },
                onCall = {
                    checkCameraAndAudioPermission(
                        context = context,
                        goToCall = { goToCall(roomId) },
                        permissionsLauncher = permissionsLauncher
                    )
                },
                onInput = { roomId = it }
            )
        }
        Text(text = "Created by yunho 2023", Modifier.padding(bottom = 10.dp))
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
