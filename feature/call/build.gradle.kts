import com.example.build_logic.setNamespace
import com.google.devtools.ksp.gradle.KspExtension

plugins {
    id("feature")
    id("com.google.devtools.ksp")
}

android {
    setNamespace("feature.call")

    extensions.configure<KspExtension> {
        arg("circuit.codegen.mode", "hilt")
    }
}

dependencies {
    implementation(projects.core.webrtc.api)
    implementation(libs.circuit.runtime)
    implementation(libs.circuit.foundation)
    implementation(libs.circuitx.android)
    implementation(libs.circuit.codegen.annotation)
    ksp(libs.circuit.codegen.ksp)
}
