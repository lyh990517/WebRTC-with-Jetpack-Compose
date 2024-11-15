import com.example.build_logic.configureHiltAndroid
import com.example.build_logic.configureKotlinAndroid
import com.example.build_logic.libs

plugins {
    id("com.android.library")
}

configureKotlinAndroid()
configureHiltAndroid()

dependencies {
    val libs = project.extensions.libs

    implementation(libs.findLibrary("google.webrtc").get())
}