package com.example.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Controller(
    onEvent: (ConnectionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(vertical = 16.dp, horizontal = 32.dp)
    ) {
        ControllerButtons(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            onEvent = onEvent
        )
    }
}
