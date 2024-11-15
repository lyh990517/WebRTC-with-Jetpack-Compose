import com.example.webrtc.configureCoroutineAndroid
import com.example.webrtc.configureHiltAndroid
import com.example.webrtc.configureKotlinAndroid

plugins {
    id("com.android.library")
    `kotlin-parcelize`
}

configureKotlinAndroid()
configureCoroutineAndroid()
configureHiltAndroid()