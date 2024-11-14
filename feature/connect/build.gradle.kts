plugins {
    id("webrtc.android.feature")
}

android {
    namespace = "com.example.connect"
}

ksp{
    arg("circuit.codegen.mode", "hilt")
}
