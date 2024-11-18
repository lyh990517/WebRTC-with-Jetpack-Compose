package com.example.controller

import android.util.Log
import com.example.common.WebRtcEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.DataChannel.Buffer
import org.webrtc.DataChannel.Observer
import org.webrtc.PeerConnection
import java.nio.ByteBuffer
import javax.inject.Inject

class DataChannelManager @Inject constructor(
    private val webRtcScope: CoroutineScope
) {
    private var dataChannel: DataChannel? = null
    private val dataChannelEvent = MutableSharedFlow<WebRtcEvent>()

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

    fun getEvent() = dataChannelEvent.asSharedFlow()

    private fun createChannelObserver() = object : Observer {
        override fun onBufferedAmountChange(p0: Long) {
            webRtcScope.launch {
                dataChannelEvent.emit(WebRtcEvent.DataChannel.OnBufferAmountChanged(p0))
            }
        }

        override fun onStateChange() {
            webRtcScope.launch {
                dataChannel?.state()?.let { state ->
                    dataChannelEvent.emit(WebRtcEvent.DataChannel.OnStateChanged(state))
                }
            }
        }

        override fun onMessage(p0: Buffer?) {
            webRtcScope.launch {
                val byteBuffer = p0?.data ?: return@launch

                val bytes = ByteArray(byteBuffer.remaining())
                byteBuffer.get(bytes)

                if (!p0.binary) {
                    val message = String(bytes, Charsets.UTF_8)

                    if (message.startsWith("{") && message.endsWith("}")) {
                        try {
                            val jsonObject = JSONObject(message)
                            dataChannelEvent.emit(
                                WebRtcEvent.DataChannel.Message.JsonObject(
                                    jsonObject
                                )
                            )
                        } catch (e: JSONException) {
                            Log.e("webrtc event", "Invalid JSON: $message")
                        }
                    } else if (message.toDoubleOrNull() != null) {
                        dataChannelEvent.emit(WebRtcEvent.DataChannel.Message.Number(message.toDouble()))
                    } else {
                        dataChannelEvent.emit(WebRtcEvent.DataChannel.Message.PlainString(message))
                    }
                } else {
                    dataChannelEvent.emit(WebRtcEvent.DataChannel.Message.File(bytes))
                }
            }
        }
    }
}