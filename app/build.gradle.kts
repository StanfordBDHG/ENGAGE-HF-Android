plugins {
    alias(libs.plugins.spezi.application)
    alias(libs.plugins.spezi.compose)
    alias(libs.plugins.spezi.hilt)
    alias(libs.plugins.spezi.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "edu.stanford.bdh.engagehf"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = (project.findProperty("android.injected.application.id") as? String) ?: "edu.stanford.bdh.engagehf"
        versionCode =
            (project.findProperty("android.injected.version.code") as? String)?.toInt() ?: 1
        versionName = (project.findProperty("android.injected.version.name") as? String) ?: "1.0.0"
        targetSdk = libs.versions.targetSdk.get().toInt()

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "USE_FIREBASE_EMULATOR", "false")
        }
        debug {
            // Disabling coverage due to: https://github.com/hapifhir/org.hl7.fhir.core/issues/1688
            enableAndroidTestCoverage = false
            buildConfigField("boolean", "USE_FIREBASE_EMULATOR", "true")
        }
    }
}

dependencies {
    implementation(project(":engagehf-modules:account"))
    implementation(project(":engagehf-modules:bluetooth"))
    implementation(project(":engagehf-modules:design"))
    implementation(project(":engagehf-modules:education"))
    implementation(project(":engagehf-modules:healthconnectonfhir"))
    implementation(project(":engagehf-modules:navigation"))
    implementation(project(":engagehf-modules:notification"))
    implementation(project(":engagehf-modules:onboarding"))

    implementation(project(":spezi:contact"))
    implementation(project(":spezi:foundation"))
    implementation(project(":spezi:core"))
    implementation(project(":spezi:core-coroutines"))
    implementation(project(":spezi:core-logging"))
    implementation(project(":spezi:ui"))

    implementation(project(":spezi:ui-personalinfo"))
    implementation(project(":spezi:questionnaire"))

    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.functions.ktx)

    implementation(libs.androidx.core.i18n)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.view.model.ktx)
    implementation(libs.androidx.splashscreen)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation.compose)
    implementation(libs.vico.compose.m3)

    implementation(libs.zxing.core)

    implementation(libs.googlecode.phonenumber)

    androidTestImplementation(project(":engagehf-modules:testing"))
    implementation(project(":spezi:testing-ui"))
    androidTestImplementation(project(":spezi:testing-ui"))
}
