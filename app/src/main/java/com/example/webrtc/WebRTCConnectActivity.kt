package com.example.webrtc

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.webrtc.databinding.ActivityWebRtcconnectBinding
import org.webrtc.*

class WebRTCConnectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebRtcconnectBinding

    private var roomID: String = "test-call"
    private var isJoin: Boolean = false

    private lateinit var rtcClient: WebRTCClient
    private val signalingClient: SignalingClient by lazy {
        initializeSignalingClient()
    }

    companion object {
        private const val TAG = "RTCActivity"
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_rtcconnect)
        getIntentData()
        checkCameraAndAudioPermission()
    }

    private fun getIntentData() {
        roomID = intent?.getStringExtra("roomID")!!
        isJoin = intent?.getBooleanExtra("isJoin", false)!!
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
        initializeSignalingClient()
    }

    private fun initializeSignalingClient() = SignalingClient(roomID, createSignalListener())

    private fun initializeRTCClient() {
        rtcClient = WebRTCClient(application, createPeerConnectionObserver())
        rtcClient.initSurfaceView(binding.remoteView)
        rtcClient.initSurfaceView(binding.localView)
        rtcClient.startLocalView(binding.localView)
        Log.e("ROOMID", "$roomID")
        if (!isJoin) rtcClient.call(roomID)
    }

    private fun createSignalListener(): SignalListener = object : SignalListener {
        override fun onOfferReceived(description: SessionDescription) {
            if (isJoin) {
                rtcClient.onRemoteSessionReceived(description)
                rtcClient.answer(roomID)
            }
        }

        override fun onAnswerReceived(description: SessionDescription) {
            if (isJoin.not()) rtcClient.onRemoteSessionReceived(description)
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcClient.addCandidate(iceCandidate)
        }

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
                signalingClient.sendIceCandidate(p0, isJoin)
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

    private fun permissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }
}