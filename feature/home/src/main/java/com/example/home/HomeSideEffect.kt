package com.example.home

sealed interface HomeSideEffect {
    data class EnterRoom(val roomId: String, val isHost: Boolean) : HomeSideEffect
    data object RoomAlreadyEnded : HomeSideEffect
}