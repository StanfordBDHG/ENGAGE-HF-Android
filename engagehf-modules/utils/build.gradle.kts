plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.spezi.base)
    alias(libs.plugins.spezi.hilt)
}

android {
    namespace = "edu.stanford.bdh.engagehf.modules.utils"

    defaultConfig {
        testInstrumentationRunner = "edu.stanford.spezi.testing.ui.HiltApplicationTestRunner"
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.unit.testing)
    androidTestImplementation(libs.bundles.unit.testing)
    androidTestImplementation(project(":spezi:testing-ui"))
}
