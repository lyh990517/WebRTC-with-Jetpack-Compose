package com.example.data.client

import com.example.domain.client.SignalingClient
import com.example.domain.repository.SignalRepository
import com.example.domain.event.SignalEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SignalingClientImpl @Inject constructor(
    private val signalRepository: SignalRepository,
) : SignalingClient,CoroutineScope {
    private lateinit var dataFlow: Flow<Map<String, Any>>
    private val eventFlow = MutableSharedFlow<SignalEvent>()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    override fun initialize(roomID: String){
        dataFlow  = signalRepository.connect(roomID)
    }
    override fun connect() = CoroutineScope(coroutineContext).launch {
        dataFlow.catch {

        }.collect { data ->
            when {
                data.containsKey("type") && data.getValue("type").toString() == "OFFER" -> handleOfferReceived(data)
                data.containsKey("type") && data.getValue("type").toString() == "ANSWER" -> handleAnswerReceived(data)
                else -> handleIceCandidateReceived(data)
            }
        }
    }

    override fun handleIceCandidateReceived(data: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            eventFlow.emit(
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

    override fun handleAnswerReceived(data: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            eventFlow.emit(
                SignalEvent.AnswerReceived(
                    SessionDescription(
                        SessionDescription.Type.ANSWER,
                        data["sdp"].toString()
                    )
                )
            )
        }
    }

    override fun handleOfferReceived(data: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            eventFlow.emit(
                SignalEvent.OfferReceived(
                    SessionDescription(
                        SessionDescription.Type.OFFER,
                        data["sdp"].toString()
                    )
                )
            )
        }
    }

    override fun getEvent(): SharedFlow<SignalEvent> = eventFlow.asSharedFlow()
}