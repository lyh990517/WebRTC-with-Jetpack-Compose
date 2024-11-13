package com.example.domain.state

sealed class FireStoreState {
    object Idle : FireStoreState()
    data class EnterRoom(val roomId: String, val isHost: Boolean) : FireStoreState()
    object RoomAlreadyEnded : FireStoreState()
}
