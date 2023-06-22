package com.example.webrtc

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentaion.view.ConnectScreen
import com.example.presentaion.view.HomeScreen

@Composable
fun WebRTCApp(navHostController: NavHostController = rememberNavController()) {
    NavHost(navController = navHostController, startDestination = Route.HOME.path) {
        composable(Route.HOME.path) {
            HomeScreen()
        }
        composable(Route.CONNECT.path) {

        }
    }
}