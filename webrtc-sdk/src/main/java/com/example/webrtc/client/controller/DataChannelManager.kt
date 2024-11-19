package com.example.webrtc.client.controller

import android.util.Log
import com.example.webrtc.client.event.WebRtcEvent
import com.example.webrtc.client.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.DataChannel
import org.webrtc.DataChannel.Buffer
import org.webrtc.PeerConnection
import java.nio.ByteBuffer
import javax.inject.Inject

class DataChannelManager @Inject constructor(
    private val webRtcScope: CoroutineScope
) {
    private var dataChannel: DataChannel? = null
    private val dataChannelEvent = MutableSharedFlow<WebRtcEvent>()
    private val messages = MutableSharedFlow<Message>()

    fun initialize(peerConnection: PeerConnection) {
        dataChannel = peerConnection.createDataChannel(
            "channel",
            DataChannel.Init().apply { negotiated = true }
        )

        dataChannel?.registerObserver(createChannelObserver())
    }

    fun sendMessage(message: String) {
        val dataChannel = dataChannel ?: return

        val buffer = ByteBuffer.wrap(message.toByteArray(Charsets.UTF_8))

        dataChannel.send(Buffer(buffer, false))
    }

    fun sendFile(bytes: ByteArray) {
        val dataChannel = dataChannel ?: return

        val buffer = ByteBuffer.wrap(bytes)

        dataChannel.send(Buffer(buffer, true))
    }

    fun getMessages() = messages.asSharedFlow()

    fun getEvent() = dataChannelEvent.asSharedFlow()

    private fun createChannelObserver() = object : DataChannel.Observer {
        override fun onBufferedAmountChange(p0: Long) {
            webRtcScope.launch {
                dataChannelEvent.emit(WebRtcEvent.StateChange.Buffer(p0))
            }
        }

        override fun onStateChange() {
            webRtcScope.launch {
                dataChannel?.state()?.let { state ->
                    dataChannelEvent.emit(WebRtcEvent.StateChange.DataChannel(state))
                }
            }
        }

        override fun onMessage(p0: Buffer?) {
            val byteBuffer = p0?.data ?: return
            val bytes = ByteArray(byteBuffer.remaining())
            byteBuffer.get(bytes)

            webRtcScope.launch {
                try {
                    if (!p0.binary) {
                        val message = String(bytes, Charsets.UTF_8)
                        messages.emit(Message.PlainString(message))
                    } else {
                        messages.emit(Message.File(bytes))
                    }
                } catch (e: Exception) {
                    Log.e("WebRTC Event", "Failed to process message: ${e.message}")
                }
            }
        }
    }
}
