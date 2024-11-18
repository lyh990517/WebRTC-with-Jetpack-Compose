import com.example.build_logic.configureCoroutineAndroid

plugins {
    id("core.base")
}

configureCoroutineAndroid()

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
}
