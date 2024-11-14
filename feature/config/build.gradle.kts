plugins {
    id("webrtc.android.feature")
    id("webrtc.android.hilt")
}
android {
    namespace = "com.example.config"
}
ksp{
    arg("circuit.codegen.mode", "hilt")
}
