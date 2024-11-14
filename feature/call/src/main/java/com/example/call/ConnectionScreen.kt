package com.example.call

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.call.ui.WebRTCController
import com.example.call.viewmodel.ConnectionViewModel
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer

const val connectionRoute = "connection"
const val roomIdArg = "roomId"
const val isHostArg = "isHost"

fun NavHostController.navigateToConnection(
    roomId: String,
    isHost: Boolean
) {
    val route = "$connectionRoute?$roomIdArg=$roomId&$isHostArg=$isHost"

    navigate(route)
}

fun NavGraphBuilder.connectionScreen(
    remoteSurface: SurfaceViewRenderer,
    localSurface: SurfaceViewRenderer
) {
    composable(
        route = "$connectionRoute?$roomIdArg={$roomIdArg}" +
                "&$isHostArg={$isHostArg}",
        arguments =
        listOf(
            navArgument(roomIdArg) {
                nullable = false
                type = NavType.StringType
            },
            navArgument(isHostArg) {
                nullable = false
                type = NavType.BoolType
            }
        )
    ) {
        ConnectionScreen(
            remoteSurface = remoteSurface,
            localSurface = localSurface
        )
    }
}

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = hiltViewModel(),
    remoteSurface: SurfaceViewRenderer,
    localSurface: SurfaceViewRenderer
) {
    LaunchedEffect(Unit) {
        delay(200)
        viewModel.connect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { remoteSurface }
        )
        AndroidView(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(0.5f)
                .align(Alignment.BottomStart)
                .padding(50.dp),
            factory = { localSurface }
        )
        WebRTCController(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            viewModel = viewModel
        )
    }
}