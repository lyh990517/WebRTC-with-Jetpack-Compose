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

    companion object {
        private const val TAG = "RTCActivity"
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebRtcconnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        collectSignalEvent()
        collectConnectionEvent()
        collectRTCEvent()
        getIntentData()
        checkCameraAndAudioPermission()

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
                            call(roomID)
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

    private fun checkCameraAndAudioPermission() {
        Log.e(TAG, "checkPermission")
        if (ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                AUDIO_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraAndAudioPermission()
        } else {
            viewModel.initRTC()
        }
    }

    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
        Log.e(TAG, "RequestPermission")
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                CAMERA_PERMISSION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                AUDIO_PERMISSION
            ) && !dialogShown
        ) {
            showPermissionRationDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(CAMERA_PERMISSION, AUDIO_PERMISSION),
                CAMERA_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showPermissionRationDialog() {
        Log.e(TAG, "showPermissionDialog")
        AlertDialog.Builder(this).apply {
            setTitle("Camera And Audio Permission Required")
            setMessage("This app need the camera and audio to function")
            setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraAndAudioPermission(true)
            }
            setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                permissionDenied()
            }
        }
    }

    private fun permissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }
}