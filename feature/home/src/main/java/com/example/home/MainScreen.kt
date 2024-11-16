package com.example.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.circuit.HomeScreen
import com.example.home.ui.SearchBar
import com.example.home.viewmodel.HomeViewModel
import com.slack.circuit.codegen.annotations.CircuitInject
import com.yunho.webrtc.core.designsystem.R
import dagger.hilt.android.components.ActivityRetainedComponent

@CircuitInject(HomeScreen::class, ActivityRetainedComponent::class)
@Composable
fun MainScreen(
    state: HomeUiState,
) {
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
                roomId = { state.roomId },
                onCall = {
                    state.eventSink(HomeEvent.Connect(true))
                },
                onJoin = {
                    state.eventSink(HomeEvent.Connect(false))
                },
                onInput = { state.eventSink(HomeEvent.Input(it)) }
            )
        }
        Text(text = "Created by yunho 2023", Modifier.padding(bottom = 10.dp))
    }
}

@Composable
fun LogoImage() {
    Image(
        modifier = Modifier.size(200.dp),
        painter = painterResource(id = R.drawable.webrtc),
        contentDescription = "Logo"
    )
}

@Composable
private fun InputContent(
    roomId: () -> String,
    onInput: (String) -> Unit,
    onCall: () -> Unit,
    onJoin: () -> Unit
) {
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        state = roomId,
        onValueChange = onInput
    ) {
        Text(text = "Room")
    }
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
        Text(text = "Create Room", color = Color.White, fontSize = 20.sp)
    }
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 40.dp),
        colors = ButtonDefaults.buttonColors(Color.Blue),
        onClick = onJoin
    ) {
        Text(text = "Join", color = Color.White, fontSize = 20.sp)
    }
}
