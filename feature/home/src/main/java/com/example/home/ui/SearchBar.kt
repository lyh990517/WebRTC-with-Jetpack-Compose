package com.example.home.ui

import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: () -> String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = state(),
        onValueChange = onValueChange,
        label = label
    )
}