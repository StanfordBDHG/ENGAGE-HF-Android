plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.spezi.base)
    alias(libs.plugins.spezi.hilt)
}

android {
    namespace = "edu.stanford.bdh.engagehf.modules.testing"
}

dependencies {
    implementation(project(":engagehf-modules:utils"))

    implementation(project(":spezi:foundation"))
    implementation(project(":spezi:core"))
    implementation(project(":spezi:core-coroutines"))
    implementation(project(":spezi:ui"))

    implementation(libs.hilt.test)
    implementation(libs.androidx.test.runner)
    implementation(libs.play.services.auth)

    api(libs.bundles.unit.testing)
    api(libs.bundles.compose.androidTest)
}
