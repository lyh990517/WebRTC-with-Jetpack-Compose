package com.example.webrtc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.presentaion.ui_component.WebRTCController
import com.example.presentaion.viewmodel.ConnectionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer

@AndroidEntryPoint
class WebRTCConnectActivity : AppCompatActivity() {


    private val viewModel: ConnectionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConnectionScreen()
        }
    }

    @Composable
    fun ConnectionScreen() {
        var remoteView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
        var localView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

        LaunchedEffect(Unit) {
            delay(200)
            if (remoteView != null && localView != null) {
                viewModel.initialize(remoteView!!, localView!!)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    SurfaceViewRenderer(it)
                },
                update = {
                    remoteView = it
                }
            )
            AndroidView(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .fillMaxWidth(0.5f)
                    .align(Alignment.BottomStart)
                    .padding(50.dp),
                factory = {
                    SurfaceViewRenderer(it)
                },
                update = {
                    localView = it
                }
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
        viewModel.closeSession()
    }

    override fun onStop() {
        super.onStop()
        viewModel.closeSession()
    }
}