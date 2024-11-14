import  com.example.build_logic.configureCoroutineAndroid
import  com.example.build_logic.configureHiltAndroid
import  com.example.build_logic.configureKotlinAndroid

plugins {
    id("com.android.library")
}

configureKotlinAndroid()
configureCoroutineAndroid()
configureHiltAndroid()

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:util"))
}
