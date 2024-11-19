package com.example.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.webrtc.core.designsystem.R

@Composable
fun LogoImage() {
    Image(
        modifier = Modifier.size(200.dp),
        painter = painterResource(id = R.drawable.webrtc),
        contentDescription = "Logo"
    )
}

@Composable
fun InputContent(
    roomId: () -> String,
    onInput: (String) -> Unit,
    onCall: () -> Unit
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        value = roomId(),
        onValueChange = onInput,
        label = {
            Text(text = "Room")
        }
    )
    Spacer(modifier = Modifier.padding(vertical = 20.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 40.dp),
        onClick = onCall,
        colors = ButtonDefaults.buttonColors(Color.Blue)
    ) {
        Icon(imageVector = Icons.Filled.Send, contentDescription = "", tint = Color.White)
        Text(text = "Connect", color = Color.White, fontSize = 20.sp)
    }
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
}
