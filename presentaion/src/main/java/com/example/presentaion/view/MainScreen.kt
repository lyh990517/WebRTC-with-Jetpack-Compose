package com.example.presentaion.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.presentaion.viewmodel.FireStoreViewModel

@Composable
fun MainScreen(viewModel: FireStoreViewModel) {
    val roomId = remember { mutableStateOf("") }
    Column() {
        SearchBar(roomId) {
            roomId.value = it
        }
        Button(onClick = { viewModel.getRoomInfo(roomId.value, false) }) {
            Text(text = "Enter")
        }
        Button(onClick = { viewModel.getRoomInfo(roomId.value, true) }) {
            Text(text = "Join")
        }
    }
}

@Composable
fun SearchBar(
    state: State<String>,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(value = state.value, onValueChange = onValueChange)
}
