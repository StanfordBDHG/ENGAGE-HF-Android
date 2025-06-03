pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ENGAGE-HF-Android"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Please keep the projects sorted. Select all method calls below and in Android Studio `Edit > Sort Lines`
include(":app")
include(":engagehf-modules:account")
include(":engagehf-modules:bluetooth")
include(":engagehf-modules:design")
include(":engagehf-modules:education")
include(":engagehf-modules:healthconnectonfhir")
include(":engagehf-modules:navigation")
include(":engagehf-modules:notification")
include(":engagehf-modules:onboarding")
include(":engagehf-modules:testing")
include(":engagehf-modules:utils")
include("spezi:contact")
include("spezi:core")
include("spezi:core-coroutines")
include("spezi:core-logging")
include("spezi:core-testing")
include("spezi:foundation")
include("spezi:questionnaire")
include("spezi:storage-credential")
include("spezi:storage-local")
include("spezi:testing-ui")
include("spezi:ui")
include("spezi:ui-markdown")
include("spezi:ui-personalinfo")
include("spezi:ui-theme")
