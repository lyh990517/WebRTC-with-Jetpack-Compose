package com.example.webrtc

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import kotlin.coroutines.CoroutineContext

class SignalingClient(
    private val roomID: String,
    private val signalListener: SignalListener
) : CoroutineScope {
    private val database = Firebase.firestore
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    private var fetchJob: Job

    init {
        fetchJob = connect()
    }

    private fun connect() = launch {

        database.enableNetwork().addOnFailureListener { e ->
            Log.e("Rsupport", "connect error : ${e.message}")
        }

        database.collection("calls").document(roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("Rsupport", "listener error : ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                when {
                    data?.containsKey("type")!! && data.getValue("type").toString() == "OFFER" -> {
                        handleOfferReceived(data)
                    }
                    data.containsKey("type") && data.getValue("type").toString() == "ANSWER" -> {
                        handleAnswerReceived(data)
                    }
                }
            }
        }

        database.collection("calls").document(roomID).collection("candidates")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Rsupport", "listener error : ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.isEmpty.not()) {
                    snapshot.forEach { dataSnapshot ->
                        val data = dataSnapshot.data
                        handleIceCandidateReceived(data)
                    }
                }
            }
    }

    fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean) = runBlocking {
        val type = if (isJoin) "answerCandidate" else "offerCandidate"

        val iceCandidate = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp,
            "type" to type
        )

        database.collection("calls").document(roomID).collection("candidates").document(type)
            .set(iceCandidate).addOnSuccessListener {
                Log.e("Rsupport", "sendIceCandidate: Success")
            }.addOnFailureListener {
                Log.e("Rsupport", "sendIceCandidate: Error $it")
            }
    }

    private fun handleIceCandidateReceived(data: Map<String, Any>) {
        signalListener.onIceCandidateReceived(
            IceCandidate(
                data["sdpMid"].toString(),
                Math.toIntExact(data["sdpMLineIndex"] as Long),
                data["sdpCandidate"].toString()
            )
        )
    }

    private fun handleAnswerReceived(data: Map<String, Any>) {
        Log.e("Rsupport", "handleAnswerReceived")
        signalListener.onAnswerReceived(
            SessionDescription(
                SessionDescription.Type.ANSWER,
                data["sdp"].toString()
            )
        )
    }

    private fun handleOfferReceived(data: Map<String, Any>) {
        Log.e("Rsupport", "handleOfferReceived")
        signalListener.onOfferReceived(
            SessionDescription(
                SessionDescription.Type.OFFER,
                data["sdp"].toString()
            )
        )
    }

    fun destroy() {
        fetchJob.cancel()
    }
}