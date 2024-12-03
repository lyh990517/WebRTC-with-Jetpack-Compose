package com.example.webrtc.sdk.model

data class Packet(
    val data: Map<String, Any>,
) {
    companion object {
        fun Packet.isOffer() = data.containsKey("type") && data.getValue("type")
            .toString() == "OFFER"

        fun Packet.isAnswer() = data.containsKey("type") && data.getValue("type")
            .toString() == "ANSWER"
    }
}
