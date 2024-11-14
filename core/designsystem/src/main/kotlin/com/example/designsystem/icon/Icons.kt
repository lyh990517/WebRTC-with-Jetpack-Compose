package com.example.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.designsystem.R

object Icons {
    val webRtc = R.drawable.webrtc
}

val Int.toPainter @Composable get() = painterResource(id = this)
