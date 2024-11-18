package com.example.controller

import android.util.Log
import org.webrtc.DataChannel
import org.webrtc.DataChannel.Buffer
import org.webrtc.DataChannel.Observer
import org.webrtc.PeerConnection
import java.nio.ByteBuffer
import javax.inject.Inject

class DataChannelManager @Inject constructor() {
    private var dataChannel: DataChannel? = null

    fun initialize(peerConnection: PeerConnection) {
        dataChannel = peerConnection.createDataChannel(
            "channel",
            DataChannel.Init().apply { negotiated = true }
        )

        dataChannel?.registerObserver(createChannelObserver())
    }

    fun sendMessage(data: Any) {
        when (data) {
            is String -> {
                dataChannel?.send(
                    Buffer(ByteBuffer.wrap(data.toByteArray()), false)
                )
            }

            else -> {
                Log.e("DataChannelManager", "todo implement")
            }
        }
    }

    private fun createChannelObserver() = object : Observer {
        override fun onBufferedAmountChange(p0: Long) {

        }

        override fun onStateChange() {

        }

        override fun onMessage(p0: Buffer?) {
            val byteBuffer = p0?.data ?: return

            val bytes = ByteArray(byteBuffer.remaining())
            byteBuffer.get(bytes)
            val message = String(bytes, Charsets.UTF_8)

            Log.e("webrtc event", "message: $message")
        }
    }
}