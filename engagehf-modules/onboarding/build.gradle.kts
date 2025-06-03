plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.compose)
    alias(libs.plugins.spezi.hilt)
}

android {
    namespace = "edu.stanford.bdh.engagehf.modules.onboarding"
}

dependencies {
    implementation(project(":engagehf-modules:account"))
    implementation(project(":engagehf-modules:design"))
    implementation(project(":engagehf-modules:navigation"))
    implementation(project(":engagehf-modules:utils"))

    implementation(project(":spezi:foundation"))
    implementation(project(":spezi:core"))
    implementation(project(":spezi:core-coroutines"))
    implementation(project(":spezi:ui"))
    implementation(project(":spezi:ui-markdown"))

    implementation(libs.androidx.foundation)
    implementation(libs.accompanist.pager)
    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.bundles.unit.testing)

    androidTestImplementation(libs.bundles.compose.androidTest)
    androidTestImplementation(project(":spezi:testing-ui"))
}
