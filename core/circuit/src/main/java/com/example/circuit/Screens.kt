package com.example.circuit

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeScreen(val roomId: String = "", val isHost: Boolean = false) : Screen

@Parcelize
data class ConnectScreen(val roomId: String, val isHost: Boolean) : Screen