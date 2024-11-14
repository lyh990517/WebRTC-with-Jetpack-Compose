package com.example.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.api.HomeState
import com.example.home.view.MainScreen
import com.example.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkCameraAndAudioPermission()
        setContent {
            MainScreen(viewModel = viewModel)
        }
        collectState()
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is HomeState.EnterRoom -> {
                        val intent =
                            Intent(this@MainActivity, WebRTCConnectActivity::class.java).apply {
                                putExtra("roomID", it.roomId)
                                putExtra("isHost", it.isHost)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        startActivity(intent)
                    }

                    is HomeState.RoomAlreadyEnded -> {
                        Toast.makeText(this@MainActivity,"이미 사용된 방입니다.",Toast.LENGTH_SHORT).show()
                    }

                    is HomeState.Idle -> {

                    }
                }
            }
        }
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
                arrayOf(
                    CAMERA_PERMISSION,
                    AUDIO_PERMISSION
                ),
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

    companion object {
        private const val TAG = "RTCActivity"
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }
}