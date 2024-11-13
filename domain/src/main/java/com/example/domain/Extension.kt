package com.example.domain

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

fun IceCandidate.parseDate(type: String) = hashMapOf(
    "serverUrl" to serverUrl,
    "sdpMid" to sdpMid,
    "sdpMLineIndex" to sdpMLineIndex,
    "sdpCandidate" to sdp,
    "type" to type
)

fun SessionDescription.parseData() = hashMapOf<String, Any>(
    "sdp" to description,
    "type" to type
)
