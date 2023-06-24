package com.example.webrtc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentaion.Route
import com.example.presentaion.view.ConnectScreen
import com.example.presentaion.view.HomeScreen
import com.example.presentaion.viewmodel.FireStoreViewModel

@Composable
fun WebRTCApp(
    navHostController: NavHostController = rememberNavController(),
    fireStoreViewModel: FireStoreViewModel
) {
    NavHost(navController = navHostController, startDestination = Route.HOME.path) {
        composable(Route.HOME.path) {
            HomeScreen(
                navController = navHostController,
                state = fireStoreViewModel.state.collectAsState(),
                onStart = { fireStoreViewModel.getRoomInfo(false) },
                onJoin = { fireStoreViewModel.getRoomInfo(true) })
        }
        composable(Route.CONNECT.path) {
            ConnectScreen()
        }
    }
}