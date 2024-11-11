import com.example.webrtc.configureCoroutineAndroid
import com.example.webrtc.configureFirebase
import com.example.webrtc.configureHiltAndroid
import com.example.webrtc.configureKotlinAndroid

plugins {
    id("com.android.library")
}

configureKotlinAndroid()
configureCoroutineAndroid()
configureHiltAndroid()
configureFirebase()