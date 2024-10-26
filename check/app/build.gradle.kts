plugins {
    alias(plugins.plugins.android.application)
    alias(plugins.plugins.kotlin.android)
}

android {
    namespace = "com.checker"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(androidx.androidx.core.ktx)
    implementation(androidx.androidx.appcompat)
//    implementation(androidx.androidx.activity)
    implementation(androidx.androidx.constraintlayout)
}