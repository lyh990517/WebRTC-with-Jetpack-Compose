package com.example.webrtc.client

import com.example.webrtc.data.SignalRepository
import com.example.webrtc.event.SignalEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import kotlin.coroutines.CoroutineContext

class SignalingClient(
    private val signalRepository: SignalRepository,
) : CoroutineScope {
    private lateinit var dataFlow: Flow<Map<String, Any>>
    private val _eventFlow = MutableSharedFlow<SignalEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    fun initialize(roomID: String){
        dataFlow  = signalRepository.connect(roomID)
    }
    fun connect() = CoroutineScope(coroutineContext).launch {
        dataFlow.catch {

        }.collect { data ->
            when {
                data.containsKey("type") && data.getValue("type").toString() == "OFFER" -> handleOfferReceived(data)
                data.containsKey("type") && data.getValue("type").toString() == "ANSWER" -> handleAnswerReceived(data)
                else -> handleIceCandidateReceived(data)
            }
        }
    }

    private fun handleIceCandidateReceived(data: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            _eventFlow.emit(
                SignalEvent.IceCandidateReceived(
                    IceCandidate(
                        data["sdpMid"].toString(),
                        Math.toIntExact(data["sdpMLineIndex"] as Long),
                        data["sdpCandidate"].toString()
                    )
                )
            )
        }
    }

    private fun handleAnswerReceived(data: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            _eventFlow.emit(
                SignalEvent.AnswerReceived(
                    SessionDescription(
                        SessionDescription.Type.ANSWER,
                        data["sdp"].toString()
                    )
                )
            )
        }
    }

    private fun handleOfferReceived(data: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            _eventFlow.emit(
                SignalEvent.OfferReceived(
                    SessionDescription(
                        SessionDescription.Type.OFFER,
                        data["sdp"].toString()
                    )
                )
            )
        }
    }
}