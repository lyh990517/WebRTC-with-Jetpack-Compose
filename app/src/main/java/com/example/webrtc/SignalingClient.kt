package com.example.webrtc

import android.util.Log
import com.example.webrtc.data.WebRTCRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SignalingClient(
    private val roomID: String,
    private val signalListener: SignalListener,
    private val webRTCRepository: WebRTCRepository
) : CoroutineScope {

    private val database = Firebase.firestore
    private val dataFlow: Flow<Map<String, Any>> = webRTCRepository.connect(roomID)
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    init {
        connect()
    }

    private fun connect() = CoroutineScope(coroutineContext).launch {
        dataFlow.catch {

        }.collect { data ->
            Log.e("data", "$data")
            when {
                data.containsKey("type") && data.getValue("type").toString() == "OFFER" -> {
                    handleOfferReceived(data)
                }

                data.containsKey("type") && data.getValue("type").toString() == "ANSWER" -> {
                    handleAnswerReceived(data)
                }

                else -> {
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
}