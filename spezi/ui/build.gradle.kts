plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.compose)
    alias(libs.plugins.spezi.hilt)
}

android {
    namespace = "edu.stanford.spezi.ui"
}

dependencies {
    api(libs.bundles.compose.androidTest)

    api(project(":spezi:ui-theme"))
    implementation(project(":spezi:core-logging"))
    implementation(project(":spezi:foundation"))
    androidTestImplementation(project(":spezi:testing-ui"))
}
