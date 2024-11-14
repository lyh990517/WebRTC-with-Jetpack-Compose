package com.example.presentaion.view

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.build_logic.com.example.presentaion.R
import com.example.presentaion.ui_component.SearchBar
import com.example.presentaion.viewmodel.FireStoreViewModel

@Composable
fun MainScreen(viewModel: FireStoreViewModel) {
    val roomId = remember { mutableStateOf("") }
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
            InputContent(roomId, viewModel)
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
    roomId: MutableState<String>,
    viewModel: FireStoreViewModel
) {
    SearchBar(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 40.dp),
        state = roomId,
        onValueChange = { roomId.value = it }) {
        Text(text = "Room")
    }
    Spacer(modifier = Modifier.padding(vertical = 20.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 40.dp),
        onClick = { viewModel.getRoomInfo(roomId.value, true) },
        colors = ButtonDefaults.buttonColors(Color.Blue)
    ) {
        Icon(imageVector = Icons.Filled.Send, contentDescription = "", tint = Color.White)
        Text(text = "Create Room", color = Color.White, fontSize = 20.sp)
    }
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
    Button(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .padding(horizontal = 40.dp),
        colors = ButtonDefaults.buttonColors(Color.Blue),
        onClick = { viewModel.getRoomInfo(roomId.value, false) }) {
        Text(text = "Join", color = Color.White, fontSize = 20.sp)
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(viewModel = viewModel())
}