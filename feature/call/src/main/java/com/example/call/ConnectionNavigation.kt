package com.example.call

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val defaultConnectionRoute = "default_connection"
const val connectionWithProjectionRoute = "connection_with_projection"
const val roomIdArg = "roomId"

fun NavHostController.navigateToMediaProjectionConnection(
    roomId: String
) {
    val route = "$connectionWithProjectionRoute?$roomIdArg=$roomId"

    navigate(route)
}

fun NavGraphBuilder.mediaProjectionConnectionScreen() {
    composable(
        route = "$connectionWithProjectionRoute?$roomIdArg={$roomIdArg}",
        arguments =
        listOf(
            navArgument(roomIdArg) {
                nullable = false
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val roomId = backStackEntry.arguments?.getString(roomIdArg)
            ?: throw IllegalArgumentException("roomId is required")

        MediaProjectionConnectionScreen(roomId = roomId)
    }
}

fun NavHostController.navigateToDefaultConnection(
    roomId: String
) {
    val route = "$defaultConnectionRoute?$roomIdArg=$roomId"

    navigate(route)
}

fun NavGraphBuilder.defaultConnectionScreen() {
    composable(
        route = "$defaultConnectionRoute?$roomIdArg={$roomIdArg}",
        arguments =
        listOf(
            navArgument(roomIdArg) {
                nullable = false
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val roomId = backStackEntry.arguments?.getString(roomIdArg)
            ?: throw IllegalArgumentException("roomId is required")

        DefaultConnectionScreen(roomId = roomId)
    }
}
