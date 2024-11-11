package com.example.webrtc

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureComposeAndroid() {
    with(plugins) {
        apply("org.jetbrains.kotlin.plugin.compose")
    }

    val libs = extensions.libs
    androidExtension.apply {
        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))

            add("implementation", libs.findLibrary("androidx.compose.material3").get())
            add("implementation", libs.findLibrary("androidx.compose.ui").get())
            add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
            add("androidTestImplementation", libs.findLibrary("androidx.test.ext").get())
            add("androidTestImplementation", libs.findLibrary("androidx.test.espresso.core").get())
            add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
        }
    }
}
