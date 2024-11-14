import com.example.webrtc.configureCircuit
import com.example.webrtc.configureHiltAndroid
import com.example.webrtc.libs


plugins {
    id("webrtc.android.library")
    id("webrtc.android.compose")
    `kotlin-parcelize`
}

android {
    packaging {
        resources {
            excludes.add("META-INF/**")
        }
    }
}

configureHiltAndroid()
configureCircuit()

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":common:screen"))
    implementation(project(":common:circuit"))
    implementation(project(":data"))
    implementation(project(":domain"))

    val libs = project.extensions.libs
    implementation(libs.findLibrary("hilt.navigation.compose").get())
    implementation(libs.findLibrary("androidx.compose.navigation").get())

    implementation(libs.findLibrary("androidx.lifecycle.viewModelCompose").get())
    implementation(libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
}