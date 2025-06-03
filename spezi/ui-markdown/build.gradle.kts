plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.compose)
    alias(libs.plugins.spezi.hilt)
}

android {
    namespace = "edu.stanford.spezi.ui.markdown"
}

dependencies {
    api(libs.bundles.compose.androidTest)

    api(project(":spezi:ui"))
    implementation(project(":spezi:core-logging"))
    androidTestImplementation(project(":spezi:testing-ui"))
}
