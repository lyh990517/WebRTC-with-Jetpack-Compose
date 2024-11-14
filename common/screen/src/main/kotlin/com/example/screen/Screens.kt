package com.example.screen

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object ConfigScreen : Screen

@Parcelize
data class ConnectScreen(val accessCode : String, val type : ConnectType) : Screen {
    enum class ConnectType {
        CREATE, JOIN
    }
}