package com.example.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.call.navigation.connectionScreen
import com.example.call.navigation.navigateToConnection
import com.example.home.navigation.homeRoute
import com.example.home.navigation.mainScreen

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
