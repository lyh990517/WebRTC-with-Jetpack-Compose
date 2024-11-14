package com.example.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.home.ui_component.WebRTCController
import com.example.home.viewmodel.ConnectionViewModel
import com.example.webrtc.api.LocalSurface
import com.example.webrtc.api.RemoteSurface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@AndroidEntryPoint
class WebRTCConnectActivity : AppCompatActivity() {


    private val viewModel: ConnectionViewModel by viewModels()

    @Inject
    @RemoteSurface
    lateinit var remoteSurface: SurfaceViewRenderer

    @Inject
    @LocalSurface
    lateinit var localSurface: SurfaceViewRenderer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConnectionScreen(
                remoteSurface = remoteSurface,
                localSurface = localSurface
            )
        }
    }

    @Composable
    fun ConnectionScreen(
        remoteSurface: SurfaceViewRenderer,
        localSurface: SurfaceViewRenderer
    ) {
        LaunchedEffect(Unit) {
            delay(200)
            viewModel.connect()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { remoteSurface }
            )
            AndroidView(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .fillMaxWidth(0.5f)
                    .align(Alignment.BottomStart)
                    .padding(50.dp),
                factory = { localSurface }
            )
            WebRTCController(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                viewModel = viewModel
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnect()
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect()
    }
}