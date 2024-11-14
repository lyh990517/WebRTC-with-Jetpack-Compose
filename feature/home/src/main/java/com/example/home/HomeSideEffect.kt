package com.example.home

import com.example.api.HomeState

sealed interface HomeSideEffect {
    data class EnterRoom(val roomId: String, val isHost: Boolean) : HomeSideEffect
    data object RoomAlreadyEnded : HomeSideEffect
}