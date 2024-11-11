import com.example.webrtc.configureComposeAndroid
import com.example.webrtc.configureCoroutineAndroid
import com.example.webrtc.configureFirebase
import com.example.webrtc.configureHiltAndroid
import com.example.webrtc.configureKotlinAndroid

plugins {
    id("com.android.application")
}
configureKotlinAndroid()
configureCoroutineAndroid()
configureHiltAndroid()
configureFirebase()
configureComposeAndroid()

