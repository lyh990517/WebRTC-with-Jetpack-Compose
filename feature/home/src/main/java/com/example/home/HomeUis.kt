package com.example.home

import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.home.checkCameraAndAudioPermission
import com.yunho.webrtc.core.designsystem.R

@Composable
fun Logo() {
    Image(
        painter = painterResource(id = R.drawable.webrtc),
        contentDescription = "Logo",
        modifier = Modifier
            .size(200.dp)
            .padding(top = 40.dp)
    )
}

@Composable
fun FooterText(modifier: Modifier = Modifier) {
    Text(
        text = "Created by yunho 2023",
        modifier = modifier
            .padding(vertical = 24.dp),
        style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
    )
}

@Composable
fun ConnectButton(
    context: Context,
    onNavResult: (HomeNavResult) -> Unit,
    roomId: String,
    permissionsLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
) {
    Button(
        onClick = {
            checkCameraAndAudioPermission(
                context = context,
                goToCall = { onNavResult(HomeNavResult.ToDefault(roomId)) },
                permissionsLauncher = permissionsLauncher
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(Color(0xFF6200EE)),
        elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
    ) {
        Text(text = "Connect", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun MediaProjectionConnectButton(
    context: Context,
    onNavResult: (HomeNavResult) -> Unit,
    roomId: String,
    permissionsLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
) {
    Button(
        onClick = {
            checkCameraAndAudioPermission(
                context = context,
                goToCall = { onNavResult(HomeNavResult.ToProjection(roomId)) },
                permissionsLauncher = permissionsLauncher
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(Color(0xFF6200EE)),
        elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
    ) {
        Text(text = "Connect With Projection", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun RoomListButton(onShow: () -> Unit) {
    Button(
        onClick = onShow,
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(Color(0xFF6200EE)),
        elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Show Rooms", color = Color.White, fontSize = 18.sp)
        }
    }
}
