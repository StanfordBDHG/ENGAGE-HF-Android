plugins {
    alias(libs.plugins.spezi.library)
}

android {
    namespace = "edu.stanford.spezi.core"
}

dependencies {
    implementation(project(":spezi:core-logging"))
    implementation(libs.kotlin.reflect)
    api(project(":spezi:foundation"))
}
