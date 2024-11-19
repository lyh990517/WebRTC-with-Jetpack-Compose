import com.example.build_logic.configureCoroutineAndroid
import com.example.build_logic.configureFirebase
import com.example.build_logic.configureHiltAndroid
import com.example.build_logic.configureKotlinAndroid
import com.example.build_logic.libs
import gradle.kotlin.dsl.accessors._2fb5859a04200edaf14b854c40b2e363.implementation
import org.gradle.kotlin.dsl.dependencies

plugins {
    id("com.android.library")
}

configureKotlinAndroid()
configureHiltAndroid()
configureCoroutineAndroid()
configureFirebase()

dependencies {
    val libs = project.extensions.libs

    implementation(libs.findLibrary("google.webrtc").get())
}