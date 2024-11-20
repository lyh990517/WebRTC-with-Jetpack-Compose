package com.example.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
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
    var isDialogVisible by remember { mutableStateOf(false) }

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
        Button(onClick = { isDialogVisible = true }) {
            Text("Show Rooms")
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

    if (isDialogVisible) {
        RoomDialog(
            rooms = viewModel.rooms,
            onDismiss = { isDialogVisible = false },
            onRoomClick = { room ->
                isDialogVisible = false
                goToCall(room)
            }
        )
    }
}

@Composable
fun RoomDialog(
    rooms: List<String>,
    onDismiss: () -> Unit,
    onRoomClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Available Rooms",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp) // 제한된 높이
            ) {
                items(rooms) { room ->
                    RoomItem(roomName = room, onClick = { onRoomClick(room) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun RoomItem(
    roomName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Join Room",
                tint = Color.White,
                modifier = Modifier.padding(10.dp).size(24.dp)
            )
        }
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
