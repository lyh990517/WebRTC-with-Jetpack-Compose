package com.example.presentaion.view

import android.content.Context
import androidx.compose.runtime.Composable

fun connectScreen(context: Context, roomId: String, isJoin: Boolean) {
    ConnectActivity.startActivity(context, roomId, isJoin)
}