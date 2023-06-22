package com.example.webrtc.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.webrtc.data.SignalRepository
import com.example.webrtc.databinding.ActivityWebRtcconnectBinding
import com.example.webrtc.event.PeerConnectionEvent
import com.example.webrtc.event.SignalEvent
import com.example.webrtc.event.WebRTCEvent
import com.example.webrtc.state.UiState
import com.example.webrtc.viewmodel.ConnectionViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WebRTCConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebRtcconnectBinding

    private val viewModel: ConnectionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebRtcconnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        collectConnectionEvent()
        collectRTCEvent()
        collectState()
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.uiState.collect{
                when(it){
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