plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.compose)
    alias(libs.plugins.spezi.hilt)
    alias(libs.plugins.spezi.serialization)
}

android {
    namespace = "edu.stanford.bdh.engagehf.modules.education"
}

dependencies {

    implementation(libs.androidyoutubeplayer.core)
    implementation(libs.hilt.navigation.compose)

    implementation(project(":engagehf-modules:design"))
    implementation(project(":engagehf-modules:navigation"))

    implementation(project(":spezi:foundation"))
    implementation(project(":spezi:core"))
    implementation(project(":spezi:core-logging"))
    implementation(project(":spezi:ui"))
    androidTestImplementation(project(":spezi:testing-ui"))
}
