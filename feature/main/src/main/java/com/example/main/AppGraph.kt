package com.example.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.call.connectionScreen
import com.example.call.navigateToConnection
import com.example.home.homeRoute
import com.example.home.mainScreen

@Composable
fun AppGraph(
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(navController = navHostController, startDestination = homeRoute) {
        mainScreen { roomId ->
            navHostController.navigateToConnection(roomId)
        }

        connectionScreen()
    }
}
