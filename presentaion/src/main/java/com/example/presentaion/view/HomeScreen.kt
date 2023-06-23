package com.example.presentaion.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.domain.state.FireStoreState
import com.example.presentaion.viewmodel.FireStoreViewModel
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    navController: NavHostController,
    state: State<FireStoreState>,
    onStart: (String) -> Unit,
    onJoin: (String) -> Unit
) {
    val roomId = rememberSaveable {
        mutableStateOf(String())
    }
    val scope = rememberCoroutineScope()
    when (state.value) {
        is FireStoreState.EnterRoom -> {
            Text(text = "${(state.value as FireStoreState.EnterRoom).isJoin}")
        }

        is FireStoreState.RoomAlreadyEnded -> {
            Text(text = "${(state.value as FireStoreState.RoomAlreadyEnded)}")
        }

        is FireStoreState.Idle -> {
            Text(text = "${(state.value)}")
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(roomId) {
            roomId.value = it
        }
        Row {
            Button(onClick = {
                scope.launch {
                    onStart(roomId.value)
                }
            }) {
                Text(text = "Start")
            }
            Button(onClick = {
                scope.launch {
                    onJoin(roomId.value)
                }
            }) {
                Text(text = "Join")
            }
        }
    }
}

@Composable
private fun SearchBar(roomId: State<String>, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = roomId.value,
        onValueChange = onValueChange,
        modifier = Modifier.background(Color.White)
    )

}