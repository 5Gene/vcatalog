plugins {
    alias(vcl.plugins.android.application)
    alias(vcl.plugins.kotlin.android)
}

android {
    namespace = "com.checker"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
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
    implementation(vcl.androidx.core.ktx)
    implementation(vcl.androidx.appcompat)
//    implementation(vcl.androidx.activity)
    implementation(vcl.androidx.constraintlayout)
    implementation(vcl.androidx.preference.ktx)
    implementation(vcl.google.material)
    implementation("pub.devrel:easypermissions:3.0.0")
}