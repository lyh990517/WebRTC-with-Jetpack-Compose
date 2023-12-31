package com.example.presentaion.ui_component

import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: State<String>,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = state.value,
        onValueChange = onValueChange,
        label = label
    )
}