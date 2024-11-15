package com.example.domain.usecase.webrtc

import Reflect
import androidx.annotation.Keep
import com.example.domain.usecase.ext.toCallbackFlow
import com.google.firebase.firestore.CollectionReference
import org.webrtc.SessionDescription
import javax.inject.Inject

class SendSdpUseCase @Inject constructor(private val callCollection: CollectionReference) {

    operator fun invoke(accessCode: String, sessionDesc: SessionDescription) =
        callCollection.document(accessCode)
            .set(Reflect.toMap(Params(sdp = sessionDesc.description, type = sessionDesc.type)))
            .toCallbackFlow()

    @Keep
    private data class Params(
        val sdp: String,
        val type: SessionDescription.Type
    )
}