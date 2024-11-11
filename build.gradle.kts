@file:Suppress("DSL_SCOPE_VIOLATION")
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.google.gms.google-services") version "4.3.14" apply false
}