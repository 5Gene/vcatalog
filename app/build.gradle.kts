plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

}

dependencies {

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.android.view)
    implementation(libs.bundles.android.basic)
    implementation(libs.bundles.android.project)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.compose.kmp)
}