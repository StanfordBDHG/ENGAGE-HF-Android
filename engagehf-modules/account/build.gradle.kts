plugins {
    alias(libs.plugins.spezi.library)
    alias(libs.plugins.spezi.compose)
    alias(libs.plugins.spezi.hilt)
}

android {
    namespace = "edu.stanford.bdh.engagehf.modules.account"

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/**.md"
        }
    }
}

dependencies {
    implementation(project(":engagehf-modules:design"))
    implementation(project(":engagehf-modules:navigation"))
    implementation(project(":engagehf-modules:utils"))

    implementation(project(":spezi:foundation"))
    implementation(project(":spezi:core"))
    implementation(project(":spezi:core-coroutines"))
    implementation(project(":spezi:core-logging"))
    implementation(project(":spezi:ui"))

    implementation(libs.hilt.navigation.compose)

    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)

    testImplementation(libs.bundles.unit.testing)
    androidTestImplementation(libs.bundles.compose.androidTest)
    androidTestImplementation(project(":spezi:testing-ui"))
}
