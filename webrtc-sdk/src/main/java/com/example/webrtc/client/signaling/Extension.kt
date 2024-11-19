package com.example.webrtc.client.signaling

import com.example.webrtc.client.model.Packet
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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
    data["sdpMLineIndex"]?.toString()?.toInt() ?: 0,
    data["sdpCandidate"].toString()
)

fun DocumentSnapshot.parseIceCandidates() = get("ices") as? List<Map<String, Any>>

fun DocumentSnapshot.toPacket() = data?.let { Packet(it) }

fun List<Map<String, Any>>.toPackets() = map { data -> Packet(data) }

fun DocumentReference.observeIce(onSend: (List<Packet>) -> Unit) =
    addSnapshotListener { snapshot, _ ->
        val candidates = snapshot?.parseIceCandidates()
        val packets = candidates?.toPackets()

        packets?.let { onSend(packets) }
    }

fun DocumentReference.observeSdp(onSend: (Packet) -> Unit) =
    addSnapshotListener { snapshot, _ ->
        val packet = snapshot?.toPacket()

        packet?.let { onSend(packet) }
    }
