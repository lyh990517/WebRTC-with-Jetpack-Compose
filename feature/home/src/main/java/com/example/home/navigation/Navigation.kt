package com.example.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.home.MainScreen

const val homeRoute = "homeRoute"

fun NavGraphBuilder.mainScreen(goToCall: (String) -> Unit) {
    composable(homeRoute) {
        MainScreen(goToCall = goToCall)
    }
}
