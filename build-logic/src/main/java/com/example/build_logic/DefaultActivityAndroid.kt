package com.example.build_logic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidActivity() {
    val libs = extensions.libs
    androidExtension.apply {
        dependencies {
            add("implementation", libs.findLibrary("androidx.core.ktx").get())
            add("implementation", libs.findLibrary("androidx.appcompat").get())
            add("implementation", libs.findLibrary("androidx.activity.compose").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.viewModelCompose").get())
            add("implementation", libs.findLibrary("kotlinx.immutable").get())
            add("implementation", libs.findLibrary("material").get())
            add("androidTestImplementation", libs.findLibrary("hilt.android.testing").get())
            add("kspAndroidTest", libs.findLibrary("hilt.android.compiler").get())
        }
    }
}