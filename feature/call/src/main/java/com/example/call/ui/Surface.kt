package com.example.call.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.SurfaceViewRenderer

@Composable
fun RemoteSurface(remoteSurface: SurfaceViewRenderer) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { remoteSurface }
    )
}

@Composable
fun LocalSurface(
    localSurface: SurfaceViewRenderer,
) {
    var localViewOffset by remember { mutableStateOf(Offset(16f, 16f)) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(100.dp)
                .offset { IntOffset(localViewOffset.x.toInt(), localViewOffset.y.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        localViewOffset += dragAmount
                    }
                }
                .clip(CircleShape),
            factory = { localSurface }
        )
    }
}