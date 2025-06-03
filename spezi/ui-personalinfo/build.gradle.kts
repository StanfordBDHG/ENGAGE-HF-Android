plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.compose)
}

android {
    namespace = "edu.stanford.spezi.ui.personalinfo"
}

dependencies {
    implementation(project(":spezi:core-logging"))

    api(project(":spezi:ui"))
    androidTestImplementation(project(":spezi:testing-ui"))
}
