package com.example.domain.state

sealed class FireStoreState {
    object Idle : FireStoreState()
    data class EnterRoom(val isJoin: Boolean) : FireStoreState()
    object RoomAlreadyEnded: FireStoreState()
}
