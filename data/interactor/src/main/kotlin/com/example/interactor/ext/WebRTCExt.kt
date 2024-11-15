package com.example.interactor.ext

import com.example.domain.usecase.webrtc.SendIceCandidateUseCase
import org.webrtc.IceCandidate

internal object Mapper {
    fun IceCandidate.toInfo(isJoin: Boolean): SendIceCandidateUseCase.IceCandidateInfo =
        SendIceCandidateUseCase.IceCandidateInfo(
            serverUrl = serverUrl,
            sdpMid = sdpMid,
            sdpMLineIndex = sdpMLineIndex,
            sdpCandidate = sdp,
            type = if (isJoin) "answerCandidate" else "offerCandidate"
        )
}