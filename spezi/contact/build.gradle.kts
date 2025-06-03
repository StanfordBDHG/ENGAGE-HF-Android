plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.compose)
}

android {
    namespace = "edu.stanford.spezi.contact"
}

dependencies {
    implementation(project(":spezi:core-logging"))
    implementation(project(":spezi:foundation"))

    api(project(":spezi:ui-personalinfo"))

    implementation(libs.androidx.core.i18n)
    implementation(libs.androidx.core.ktx)

    androidTestImplementation(libs.hilt.test)
    androidTestImplementation(project(":spezi:testing-ui"))
}
