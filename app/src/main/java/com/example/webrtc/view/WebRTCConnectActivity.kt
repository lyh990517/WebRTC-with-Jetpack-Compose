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
import com.example.webrtc.viewmodel.ConnectionViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WebRTCConnectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebRtcconnectBinding

    @Inject
    lateinit var signalRepository: SignalRepository

    @Inject
    lateinit var firestore: FirebaseFirestore

    private val viewModel: ConnectionViewModel by viewModels()

    private var roomID: String = "test-call"
    private var isJoin: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebRtcconnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        collectSignalEvent()
        collectConnectionEvent()
        collectRTCEvent()
        getIntentData()
        viewModel.initRTC()

    }

    private fun collectRTCEvent() {
        lifecycleScope.launch {
            viewModel.webRTCEvent.collect {
                when (it) {
                    is WebRTCEvent.Initialize -> {
                        Log.e("rtc","initialize")
                        it.webRTCClient.apply {
                            initPeerConnectionFactory(application)
                            initVideoCapture(application)
                            initSurfaceView(binding.remoteView)
                            initSurfaceView(binding.localView)
                            startLocalView(binding.localView)
                            if(!isJoin) call(roomID)
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
                        Log.e("peerConnectionEvent","OnIceCandidate")
                        viewModel.sendIceCandidate(it.data, isJoin, roomID)
                        viewModel.addCandidate(it.data)
                    }

                    is PeerConnectionEvent.OnAddStream -> {
                        Log.e("peerConnectionEvent","OnAddStream")
                        it.data.videoTracks?.get(0)?.addSink(binding.remoteView)
                    }
                }
            }
        }
    }

    private fun collectSignalEvent() {
        lifecycleScope.launch {
            viewModel.signalEvent.collect {
                Log.e("event", "$it")
                when (it) {
                    is SignalEvent.OfferReceived -> {
                        if (isJoin) {
                            Log.e("signalEvent","OfferReceived")
                            viewModel.onRemoteSessionReceived(it.data)
                            viewModel.answer(roomID)
                        }
                    }

                    is SignalEvent.AnswerReceived -> {
                        Log.e("signalEvent","AnswerReceived")
                        if (isJoin.not()) viewModel.onRemoteSessionReceived(it.data)
                    }

                    is SignalEvent.IceCandidateReceived -> {
                        Log.e("signalEvent","IceCandidateReceived")
                        viewModel.addCandidate(it.data)
                    }
                }
            }
        }
    }

    private fun getIntentData() {
        roomID = intent?.getStringExtra("roomID")!!
        isJoin = intent?.getBooleanExtra("isJoin", false)!!
        viewModel.initSignal(roomID)
    }
}