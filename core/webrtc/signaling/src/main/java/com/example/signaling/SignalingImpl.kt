package com.example.signaling

import com.example.common.WebRtcEvent
import com.example.model.RoomStatus
import com.example.model.SignalType
import com.example.webrtc.client.Signaling
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SignalingImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sdpManager: SdpManager,
    private val iceManager: IceManager
) : Signaling {

    override suspend fun getRoomStatus(roomID: String): RoomStatus {
        val data = getRoom(roomID)
            .get()
            .await()

        val isRoomEnded = data["type"] == "END_CALL"
        val roomStatus = if (isRoomEnded) RoomStatus.TERMINATED else RoomStatus.NEW

        return roomStatus
    }

    override suspend fun getRoomExists(roomID: String): Boolean {
        val room = getRoom(roomID)
        val roomExists = room.get().await().exists()

        if (!roomExists) {
            room.set(mapOf("startedAt" to "${LocalDateTime.now()}"))
        }

        return roomExists
    }

    override suspend fun sendIce(
        ice: IceCandidate?,
        type: SignalType
    ) {
        iceManager.sendIce(ice, type)
    }

    override suspend fun sendSdp(
        sdp: SessionDescription
    ) {
        sdpManager.sendSdp(sdp)
    }

    override suspend fun start(roomID: String, isHost: Boolean) {
        firestore.enableNetwork()

        sdpManager.processSdpExchange(isHost, roomID)

        iceManager.processIceExchange(isHost, roomID)
    }

    override fun getEvent(): Flow<WebRtcEvent> = merge(sdpManager.getEvent(), iceManager.getEvent())

    private fun getRoom(roomId: String) = firestore
        .collection(ROOT)
        .document(roomId)

    companion object {
        const val ROOT = "calls"
    }
}
