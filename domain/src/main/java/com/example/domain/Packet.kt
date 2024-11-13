package com.example.domain

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

data class Packet(
    val data: Map<String, Any>
) {
    companion object {
        fun Packet.isOffer() = data.containsKey("type") && data.getValue("type")
            .toString() == "OFFER"

        fun Packet.isAnswer() = data.containsKey("type") && data.getValue("type")
            .toString() == "ANSWER"

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
            Math.toIntExact(data["sdpMLineIndex"] as Long),
            data["sdpCandidate"].toString()
        )
    }
}
