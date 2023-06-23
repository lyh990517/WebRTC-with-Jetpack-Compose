package com.example.webrtc

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presentaion.Route
import com.example.presentaion.view.HomeScreen
import com.example.presentaion.view.connectScreen
import com.example.presentaion.viewmodel.FireStoreViewModel

@Composable
fun WebRTCApp(
    context: Context,
    navHostController: NavHostController = rememberNavController(),
    fireStoreViewModel: FireStoreViewModel
) {
    NavHost(navController = navHostController, startDestination = Route.HOME.path) {
        composable(route = Route.HOME.path) {
            HomeScreen(
                navController = navHostController,
                state = fireStoreViewModel.state.collectAsState(),
                onStart = { fireStoreViewModel.getRoomInfo(it, false) },
                onJoin = { fireStoreViewModel.getRoomInfo(it, true) })
        }
        composable(
            route = "${Route.CONNECT.path}/{roomId}/{isJoin}",
            listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("isJoin") { type = NavType.BoolType }
            )
        ) {
            connectScreen(
                context = context,
                roomId = it.arguments?.getString("roomId") ?: "",
                isJoin = it.arguments?.getBoolean("isJoin") ?: false
            )
        }
    }
}