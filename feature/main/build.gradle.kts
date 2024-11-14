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
    implementation(project(":feature:home"))
    implementation(project(":core:webrtc:api"))
    implementation(project(":core:firestore:api"))

    implementation(libs.google.webrtc)
}