package com.example.domain.usecase.ext

import com.example.domain.usecase.webrtc.SignalEvent
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

fun <T> Task<T>.toCallbackFlow() = callbackFlow {
    addOnSuccessListener {
        trySend(true)
    }.addOnFailureListener {
        trySend(false)
    }
    awaitClose()
}


internal fun DocumentReference.observeDocument() = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            trySend(mapOf("error" to error))
        }
        if (snapshot != null && snapshot.exists()) {
            snapshot.data?.let {
                trySend(it)
            }
        }
    }
    awaitClose { registration.remove() }
}

internal fun CollectionReference.observeCollection() = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            trySend(mapOf("error" to error))
        }

        if (snapshot != null && snapshot.isEmpty.not()) {
            snapshot.forEach { dataSnapshot ->
                trySend(dataSnapshot.data)
            }
        }
    }
    awaitClose { registration.remove() }
}

internal fun toSignalEvent(data: Map<String, Any>): SignalEvent =
    when {
        data.containsKey("type") && data.getValue("type").toString() == "OFFER" ->
            SignalEvent.OfferReceived(
                SessionDescription(
                    SessionDescription.Type.OFFER,
                    data["sdp"].toString()
                )
            )

        data.containsKey("type") && data.getValue("type").toString() == "ANSWER" ->
            SignalEvent.AnswerReceived(
                SessionDescription(
                    SessionDescription.Type.ANSWER,
                    data["sdp"].toString()
                )
            )

        else -> SignalEvent.IceCandidateReceived(
            IceCandidate(
                data["sdpMid"].toString(),
                Math.toIntExact(data["sdpMLineIndex"] as Long),
                data["sdpCandidate"].toString()
            )
        )
    }