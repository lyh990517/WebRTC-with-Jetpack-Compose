package com.example.presentaion.ui_component

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MenuButton(
    imageVector: ImageVector,
    description: String,
    defaultBackgroundColor: Color,
    state: State<Boolean> = mutableStateOf(true),
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        backgroundColor = if (state.value) defaultBackgroundColor else Color.Gray
    ) {
        Icon(imageVector = imageVector, contentDescription = description)
    }
}

@Composable
@Preview
fun MenuPreview() {
    val isClicked = rememberSaveable { mutableStateOf(false) }
    MenuButton(
        imageVector = Icons.Default.Face,
        description = "Face",
        defaultBackgroundColor = Color.Green,
        state = isClicked
    ) {

    }
}