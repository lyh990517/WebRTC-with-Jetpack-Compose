package com.example.webrtc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.event.PeerConnectionEvent
import com.example.domain.event.WebRTCEvent
import com.example.domain.state.UiState
import com.example.presentaion.ui_component.WebRTCController
import com.example.presentaion.viewmodel.ConnectionViewModel
import com.example.webrtc.databinding.ActivityWebRtcconnectBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WebRTCConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebRtcconnectBinding

    private val viewModel: ConnectionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebRtcconnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCompose()
        collectConnectionEvent()
        collectRTCEvent()
        collectState()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeSession()
    }

    override fun onStop() {
        super.onStop()
        viewModel.closeSession()
    }

    private fun setCompose() {
        binding.composeView.apply {
            setContent {
                WebRTCController(viewModel = viewModel)
            }
        }
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.uiState.collect {
                when (it) {
                    is UiState.UnInitialized -> viewModel.initRTC()
                }
            }
        }
    }

    private fun collectRTCEvent() {
        lifecycleScope.launch {
            viewModel.webRTCEvent.collect {
                when (it) {
                    is WebRTCEvent.Initialize -> {
                        it.webRTCClient.apply {
                            initPeerConnectionFactory(application)
                            initVideoCapture(application)
                            initSurfaceView(binding.remoteView)
                            initSurfaceView(binding.localView)
                            startLocalView(binding.localView)
                            viewModel.call()
                            viewModel.connect()
                        }
                    }
                    is WebRTCEvent.CloseSession -> finish()
                }
            }
        }
    }

    private fun collectConnectionEvent() {
        lifecycleScope.launch {
            viewModel.peerConnectionEvent.collect {
                when (it) {
                    is PeerConnectionEvent.OnIceCandidate -> {
                        viewModel.sendIceCandidate(it.data)
                        viewModel.addCandidate(it.data)
                    }

                    is PeerConnectionEvent.OnAddStream -> {
                        it.data.videoTracks?.get(0)?.addSink(binding.remoteView)
                    }
                }
            }
        }
    }
}