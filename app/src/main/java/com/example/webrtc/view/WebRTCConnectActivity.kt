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
import com.example.webrtc.client.WebRTCClient
import com.example.webrtc.data.WebRTCRepository
import com.example.webrtc.databinding.ActivityWebRtcconnectBinding
import com.example.webrtc.event.SignalEvent
import com.example.webrtc.viewmodel.ConnectionViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.webrtc.*
import javax.inject.Inject

@AndroidEntryPoint
class WebRTCConnectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebRtcconnectBinding
    @Inject
    lateinit var webRTCRepository: WebRTCRepository
    @Inject
    lateinit var firestore: FirebaseFirestore

    private val viewModel: ConnectionViewModel by viewModels()

    private var roomID: String = "test-call"
    private var isJoin: Boolean = false

    private lateinit var rtcClient: WebRTCClient

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
        lifecycleScope.launch {
            viewModel.eventFlow.collect{
                Log.e("event","$it")
                when(it){
                    is SignalEvent.OfferReceived -> {
                        if (isJoin) {
                            rtcClient.onRemoteSessionReceived(it.data)
                            rtcClient.answer(roomID)
                        }
                    }
                    is SignalEvent.AnswerReceived -> {
                        if (isJoin.not()) rtcClient.onRemoteSessionReceived(it.data)
                    }
                    is SignalEvent.IceCandidateReceived -> {
                        rtcClient.addCandidate(it.data)
                    }
                }
            }
        }
        getIntentData()
        checkCameraAndAudioPermission()

    }

    private fun getIntentData() {
        roomID = intent?.getStringExtra("roomID")!!
        isJoin = intent?.getBooleanExtra("isJoin", false)!!
        viewModel.init(roomID)
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
            initializeClient()
        }
    }

    private fun initializeClient() {
        initializeRTCClient()
    }


    private fun initializeRTCClient() {
        rtcClient = WebRTCClient(application, createPeerConnectionObserver())
        rtcClient.initSurfaceView(binding.remoteView)
        rtcClient.initSurfaceView(binding.localView)
        rtcClient.startLocalView(binding.localView)
        Log.e("ROOMID", "$roomID")
        if (!isJoin) rtcClient.call(roomID)
        viewModel.connect()
    }

    private fun createPeerConnectionObserver(): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                Log.e("Rsupport", "onSignalingChange")
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                Log.e("Rsupport", "onIceConnectionChange")
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {
                Log.e("Rsupport", "onIceConnectionReceivingChange")
            }

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                Log.e("Rsupport", "onIceGatheringChange")
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                Log.e("Rsupport", "onIceCandidate : $p0")
                sendIceCandidate(p0, isJoin)
                rtcClient.addCandidate(p0)
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                Log.e("Rsupport", "onIceCandidatesRemoved")
            }

            override fun onAddStream(p0: MediaStream?) {
                Log.e("Rsupport", "onAddStream")
                p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
            }

            override fun onRemoveStream(p0: MediaStream?) {
                Log.e("Rsupport", "onRemoveStream")
            }

            override fun onDataChannel(p0: DataChannel?) {
                Log.e("Rsupport", "onDataChannel")
            }

            override fun onRenegotiationNeeded() {
                Log.e("Rsupport", "onRenegotiationNeeded")
            }

            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                Log.e("Rsupport", "onAddTrack")
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

    fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean) = runBlocking {
        val type = if (isJoin) "answerCandidate" else "offerCandidate"

        val iceCandidate = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp,
            "type" to type
        )

        firestore.collection("calls").document(roomID).collection("candidates").document(type)
            .set(iceCandidate).addOnSuccessListener {
                Log.e("Rsupport", "sendIceCandidate: Success")
            }.addOnFailureListener {
                Log.e("Rsupport", "sendIceCandidate: Error $it")
            }
    }

    private fun permissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }
}