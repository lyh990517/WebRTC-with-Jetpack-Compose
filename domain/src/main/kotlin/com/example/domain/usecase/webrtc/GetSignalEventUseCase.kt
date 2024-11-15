package com.example.domain.usecase.webrtc

import com.example.domain.usecase.ext.observeCollection
import com.example.domain.usecase.ext.observeDocument
import com.example.domain.usecase.ext.toSignalEvent
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

class GetSignalEventUseCase @Inject constructor(
    private val callCollection: CollectionReference,
) {
    operator fun invoke(accessCode: String) =
        merge(
            callCollection.document(accessCode).observeDocument(),
            callCollection.document(accessCode).collection("candidates").observeCollection()
        ).map(::toSignalEvent)
}


sealed interface SignalEvent {
    data class IceCandidateReceived(val data: IceCandidate) : SignalEvent
    data class AnswerReceived(val data: SessionDescription) : SignalEvent
    data class OfferReceived(val data: SessionDescription) : SignalEvent
}