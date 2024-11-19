plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidHilt") {
            id = "webrtc.android.hilt"
            implementationClass = "com.example.webrtc.HiltAndroidPlugin"
        }
        register("kotlinHilt") {
            id = "webrtc.kotlin.hilt"
            implementationClass = "com.example.webrtc.HiltKotlinPlugin"
        }
        register("firebase") {
            id = "webrtc.firebase"
            implementationClass = "com.example.webrtc.FirebasePlugin"
        }
    }
}
