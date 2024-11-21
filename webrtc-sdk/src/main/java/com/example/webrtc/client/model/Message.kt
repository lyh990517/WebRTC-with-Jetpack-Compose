package com.example.webrtc.client.model

import android.graphics.Bitmap

sealed interface Message {
    data class PlainString(val data: String) : Message
    data class Image(val bitmaps: Bitmap) : Message
    data class File(val file: java.io.File) : Message
}