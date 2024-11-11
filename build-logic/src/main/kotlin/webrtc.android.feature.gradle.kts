import com.example.webrtc.configureFirebase
import com.example.webrtc.configureHiltAndroid
import com.example.webrtc.libs


plugins {
    id("webrtc.android.library")
    id("webrtc.android.compose")
}

android {
    packaging {
        resources {
            excludes.add("META-INF/**")
        }
    }
}

configureHiltAndroid()
configureFirebase()


dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))

    val libs = project.extensions.libs
    implementation(libs.findLibrary("hilt.navigation.compose").get())
    implementation(libs.findLibrary("androidx.compose.navigation").get())

    implementation(libs.findLibrary("androidx.lifecycle.viewModelCompose").get())
    implementation(libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
}