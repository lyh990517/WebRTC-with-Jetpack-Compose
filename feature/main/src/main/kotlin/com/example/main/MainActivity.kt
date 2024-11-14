package com.example.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.designsystem.theme.WebRTCTheme
import com.example.screen.ConfigScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var circuit: Circuit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WebRTCTheme(darkTheme = false) {
                val backStack = rememberSaveableBackStack(root = ConfigScreen)
                val navigator = rememberCircuitNavigator(backStack)
                CircuitCompositionLocals(circuit) {
                    NavigableCircuitContent(
                        navigator = navigator,
                        backStack = backStack
                    )
                }
            }
        }
    }

//    private fun checkCameraAndAudioPermission() {
//        Log.e(TAG, "checkPermission")
//        if (ContextCompat.checkSelfPermission(
//                this,
//                CAMERA_PERMISSION
//            ) != PackageManager.PERMISSION_GRANTED
//            && ContextCompat.checkSelfPermission(
//                this,
//                AUDIO_PERMISSION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestCameraAndAudioPermission()
//        }
//    }
//
//    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
//        Log.e(TAG, "RequestPermission")
//        if (ActivityCompat.shouldShowRequestPermissionRationale(
//                this,
//                CAMERA_PERMISSION
//            ) && ActivityCompat.shouldShowRequestPermissionRationale(
//                this,
//                AUDIO_PERMISSION
//            ) && !dialogShown
//        ) {
//            showPermissionRationDialog()
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(
//                    CAMERA_PERMISSION,
//                    AUDIO_PERMISSION
//                ),
//                CAMERA_AUDIO_PERMISSION_REQUEST_CODE
//            )
//        }
//    }
//
//    private fun showPermissionRationDialog() {
//        Log.e(TAG, "showPermissionDialog")
//        AlertDialog.Builder(this).apply {
//            setTitle("Camera And Audio Permission Required")
//            setMessage("This app need the camera and audio to function")
//            setPositiveButton("Grant") { dialog, _ ->
//                dialog.dismiss()
//                requestCameraAndAudioPermission(true)
//            }
//            setNegativeButton("Deny") { dialog, _ ->
//                dialog.dismiss()
//                permissionDenied()
//            }
//        }
//    }
//
//    private fun permissionDenied() {
//        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
//    }
//
//    companion object {
//        private const val TAG = "RTCActivity"
//        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
//        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
//        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
//    }
}