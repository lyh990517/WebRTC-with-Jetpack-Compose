import com.example.webrtc.configureCircuit
import com.example.webrtc.configureHiltAndroid


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
    implementation(project(":common:screen"))
    implementation(project(":common:circuit"))

    implementation(project(":core:designsystem"))

    implementation(project(":data:firebase"))
    implementation(project(":data:interactor"))

    implementation(project(":domain"))
}