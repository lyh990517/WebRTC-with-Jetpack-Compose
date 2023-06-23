package com.example.webrtc

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.presentaion.viewmodel.FireStoreViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeActivity : AppCompatActivity() {

    private val fireStoreViewModel: FireStoreViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebRTCApp(fireStoreViewModel = fireStoreViewModel)
        }
    }
}