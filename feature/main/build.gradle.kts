import com.example.build_logic.setNamespace

plugins {
    id("feature")
    id("activity")
    id("firebase")
}

android {
    setNamespace("com.example.main")
}

dependencies {
    implementation(project(":presentaion"))

    implementation(libs.google.webrtc)
}