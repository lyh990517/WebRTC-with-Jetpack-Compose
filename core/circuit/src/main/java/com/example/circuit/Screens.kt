package com.example.circuit

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object HomeScreen : Screen

@Parcelize
data class ConnectScreen(val roomId: String, val isHost: Boolean) : Screen