package com.example.webrtc.client.controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
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

    fun sendImage(bitmap: Bitmap) {
        val dataChannel = dataChannel ?: return

        val payload = bitmapToByteArray(bitmap)
        val packet = createPacket("img", payload)
        val buffer = ByteBuffer.wrap(packet)

        val data = Buffer(buffer, true)
        dataChannel.send(data)
    }

    fun sendFile(bytes: ByteArray) {
        val dataChannel = dataChannel ?: return

        val buffer = ByteBuffer.wrap(bytes)

        dataChannel.send(Buffer(buffer, true))
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun createPacket(type: String, payload: ByteArray): ByteArray {
        val typeBytes = type.toByteArray(Charsets.UTF_8)
        val typeLength = 4

        val header = ByteBuffer.allocate(typeLength + 4)
        header.put(typeBytes.copyOf(typeLength))
        header.putInt(payload.size)

        return header.array() + payload
    }


    fun parsePacket(data: ByteBuffer): Pair<String, ByteArray> {
        val typeBytes = ByteArray(4)
        data.get(typeBytes)
        val type = String(typeBytes).trim()

        val payloadLength = data.int

        val payload = ByteArray(payloadLength)
        data.get(payload)

        return Pair(type, payload)
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

        override fun onMessage(buffer: Buffer?) {
            val byteBuffer = buffer?.data ?: return
            val bytes = ByteArray(byteBuffer.remaining())
            byteBuffer.get(bytes)

            try {
                if (!buffer.binary) {
                    val message = String(bytes, Charsets.UTF_8)
                    webRtcScope.launch {
                        messages.emit(Message.PlainString(message))
                    }
                } else {
                    val data = buffer.data
                    val (type, payload) = parsePacket(data)

                    when (type) {
                        "img" -> {
                            val bitmap = byteArrayToBitmap(payload)

                            webRtcScope.launch {
                                messages.emit(Message.Image(bitmap))
                            }
                        }

                        "file" -> {

                        }

                        else -> {

                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebRTC Event", "Failed to process message: ${e.message}")
            }
        }
    }
}
