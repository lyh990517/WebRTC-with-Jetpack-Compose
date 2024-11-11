plugins {
    id("webrtc.android.feature")
    id("webrtc.android.hilt")
}

android {
    namespace = "com.example.presentaion"
}

dependencies {
    implementation("org.webrtc:google-webrtc:1.0.30039@aar")
    implementation(libs.androidx.activity.compose)
}
