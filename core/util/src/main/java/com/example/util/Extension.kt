package com.example.util

import com.example.model.Packet
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

fun Packet.toOfferSdp() = SessionDescription(
    SessionDescription.Type.OFFER,
    data["sdp"].toString()
)

fun Packet.toAnswerSdp() = SessionDescription(
    SessionDescription.Type.ANSWER,
    data["sdp"].toString()
)

fun Packet.toIceCandidate() = IceCandidate(
    data["sdpMid"].toString(),
    java.lang.Math.toIntExact(data["sdpMLineIndex"] as Long),
    data["sdpCandidate"].toString()
)
