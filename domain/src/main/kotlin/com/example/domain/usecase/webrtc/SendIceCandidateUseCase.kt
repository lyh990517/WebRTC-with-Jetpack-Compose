package com.example.domain.usecase.webrtc

import Reflect
import androidx.annotation.Keep
import com.example.domain.usecase.ext.toCallbackFlow
import com.google.firebase.firestore.CollectionReference
import javax.inject.Inject

class SendIceCandidateUseCase @Inject constructor(private val callCollection: CollectionReference) {

    operator fun invoke(accessCode: String, iceCandidateInfo: IceCandidateInfo) =
        callCollection.document(accessCode).collection("candidates")
            .document(iceCandidateInfo.type)
            .set(Reflect.toMap(iceCandidateInfo)).toCallbackFlow()

    @Keep
    data class IceCandidateInfo(
        val serverUrl: String,
        val sdpMid: String,
        val sdpMLineIndex: Int,
        val sdpCandidate: String,
        val type: String
    )
}

