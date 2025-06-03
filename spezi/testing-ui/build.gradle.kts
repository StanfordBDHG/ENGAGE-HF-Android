plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.compose)
    alias(libs.plugins.spezi.hilt)
}

android {
    namespace = "edu.stanford.spezi.testing.ui"
}

dependencies {
    api(libs.bundles.compose.androidTest)

    api(project(":spezi:ui"))
    implementation(project(":spezi:core-logging"))

    implementation(libs.hilt.test)
    implementation(libs.androidx.test.runner)

    api(libs.bundles.unit.testing)
    api(libs.bundles.compose.androidTest)
}
