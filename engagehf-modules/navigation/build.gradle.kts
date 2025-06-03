plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.hilt)
}

android {
    namespace = "edu.stanford.bdh.engagehf.modules.navigation"
}

dependencies {
    implementation(project(":spezi:foundation"))
    implementation(project(":spezi:core"))
    implementation(project(":spezi:core-coroutines"))
    implementation(project(":spezi:core-logging"))

    implementation(libs.androidx.core.ktx)
}
