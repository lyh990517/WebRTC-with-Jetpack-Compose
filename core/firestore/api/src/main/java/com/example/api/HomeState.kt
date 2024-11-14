package com.example.api

sealed class HomeState {
    object Idle : HomeState()
    data class EnterRoom(val roomId: String, val isHost: Boolean) : HomeState()
    object RoomAlreadyEnded : HomeState()
}
