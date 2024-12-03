package com.example.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.call.defaultConnectionScreen
import com.example.call.mediaProjectionConnectionScreen
import com.example.call.navigateToDefaultConnection
import com.example.call.navigateToMediaProjectionConnection
import com.example.home.HomeNavResult
import com.example.home.homeRoute
import com.example.home.homeScreen

@Composable
fun AppGraph(
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(navController = navHostController, startDestination = homeRoute) {
        homeScreen { result ->
            when (result) {
                is HomeNavResult.ToDefault -> {
                    navHostController.navigateToDefaultConnection(result.roomId)
                }

                is HomeNavResult.ToProjection -> {
                    navHostController.navigateToMediaProjectionConnection(result.roomId)
                }
            }
        }

        mediaProjectionConnectionScreen()

        defaultConnectionScreen()
    }
}
