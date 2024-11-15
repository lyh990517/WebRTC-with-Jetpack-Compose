package com.example.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.SurfaceViewRenderer

@Composable
fun WebRTCView(
    modifier: Modifier = Modifier,
    surfaceViewRenderer: SurfaceViewRenderer,
) {
    AndroidView(
        modifier = modifier,
        factory = { surfaceViewRenderer },
        update = { view ->
            view.requestLayout()
            view.invalidate()
        },
        onRelease = { view ->
            view.release()
        }
    )
}
