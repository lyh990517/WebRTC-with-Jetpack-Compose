package com.example.webrtc.sdk.mediaprojection

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.webrtc.sdk.api.WebRtcClient
import com.example.webrtc.sdk.factory.WebRtcClientFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WebRtcCaptureService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mediaProjectionIntent = intent?.getParcelableExtra<Intent>("mediaProjection")

        if (mediaProjectionIntent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        webRtcClient.update {
            WebRtcClientFactory.create(this, mediaProjectionIntent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = NotificationCompat.Builder(applicationContext, "CaptureService")
                .setContentText("On Capture...")
                .setContentTitle("CaptureService")
                .build()

            startForeground(1, notification)
        }

        webRtcClient.value?.startCapture()

        return START_NOT_STICKY
    }

    companion object {
        private var webRtcClient = MutableStateFlow<WebRtcClient?>(null)

        fun getClient() = webRtcClient.asStateFlow()

        fun stopService(context: Context) {
            webRtcClient.value?.disconnect()
            webRtcClient.update { null }

            val intent = Intent(context, WebRtcCaptureService::class.java)
            context.stopService(intent)
        }

        fun startService(context: Context, mediaProjectionIntent: Intent) {
            val intent = Intent(context, WebRtcCaptureService::class.java).apply {
                putExtra("mediaProjection", mediaProjectionIntent)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            }
        }
    }
}
