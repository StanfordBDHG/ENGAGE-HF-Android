plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spezi.compose)
    alias(libs.plugins.spezi.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "edu.stanford.bdh.engagehf.modules.notification"
}

dependencies {
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":engagehf-modules:account"))
    implementation(project(":engagehf-modules:design"))
    implementation(project(":engagehf-modules:navigation"))

    implementation(project(":spezi:foundation"))
    implementation(project(":spezi:core"))
    implementation(project(":spezi:core-coroutines"))
    implementation(project(":spezi:core-logging"))
    implementation(project(":spezi:ui"))
    implementation(project(":spezi:storage-credential"))
    androidTestImplementation(project(":spezi:testing-ui"))
}
