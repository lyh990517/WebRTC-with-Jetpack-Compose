package com.example.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.home.HomeScreen

const val homeRoute = "homeRoute"

sealed interface HomeNavResult {
    data class ToDefault(val roomId: String) : HomeNavResult
    data class ToProjection(val roomId: String) : HomeNavResult
}

fun NavGraphBuilder.homeScreen(
    onNavResult: (HomeNavResult) -> Unit
) {
    composable(homeRoute) {
        HomeScreen(onNavResult)
    }
}
