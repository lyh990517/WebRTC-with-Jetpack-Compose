package com.example.webrtc.client.model

import org.json.JSONObject

sealed interface Message {
    data class PlainString(val data: String) : Message
    data class Json(val jsonObject: JSONObject) : Message
    data class File(val bytes: ByteArray) : Message {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as File

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
}