plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        register("androidHilt") {
            id = "android.hilt"
            implementationClass = "com.example.build_logic.HiltAndroidPlugin"
        }
        register("kotlinHilt") {
            id = "kotlin.hilt"
            implementationClass = "com.example.build_logic.HiltKotlinPlugin"
        }
    }
}