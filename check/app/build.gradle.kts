import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(vcl.plugins.android.application)
    alias(vcl.plugins.gene.android)
//    alias(vcl.plugins.compose.compiler)
    alias(vcl.plugins.ksp)
//    alias(vcl.plugins.hilt)
//    alias(vcl.plugins.kotlin.serialization)
}
//    apply<MyPlugin>()

android {
    namespace = "com.checker"


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
//    buildFeatures {
//        compose = true
//    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
        }
    }
}

dependencies {
    // wings.versions.toml [libraries] — catalog 依赖解析冒烟（不含 Gradle 插件 artifact）

    // --- Gene ---
    implementation(vcl.gene.cubic)
    implementation(vcl.gene.cartoon)
    implementation(vcl.gene.adapter)
    implementation(vcl.gene.view.dsl)
    implementation(vcl.gene.ksp.poe)
    implementation(vcl.gene.retrofit.ksp.anno)
//    ksp(vcl.gene.retrofit.ksp)

    // --- AndroidX 核心 / KTX ---
    implementation(vcl.androidx.core.ktx)
    implementation(vcl.androidx.annotation)
    implementation(vcl.androidx.activity.ktx)
    implementation(vcl.androidx.fragment.ktx)
    implementation(vcl.androidx.collection.ktx)
    implementation(vcl.androidx.preference.ktx)
    implementation(vcl.androidx.transition.ktx)
    implementation(vcl.androidx.work.runtime.ktx)
    implementation(vcl.androidx.work.runtime)
    implementation(vcl.androidx.palette.ktx)
    implementation(vcl.androidx.core.animation)
    implementation(vcl.androidx.core.splashscreen)
    implementation(vcl.androidx.profileinstaller)
    implementation(vcl.androidx.window)
    implementation(vcl.androidx.window.ext)
    implementation(vcl.androidx.core.pip)
    implementation(vcl.androidx.bluetooth)
    implementation(vcl.androidx.emoji2.views)

    // --- Lifecycle ---
    implementation(vcl.androidx.lifecycle.runtime.ktx)
    implementation(vcl.androidx.lifecycle.livedata.ktx)
    implementation(vcl.androidx.lifecycle.viewmodel.ktx)
    implementation(vcl.androidx.lifecycle.viewmodel.savedstate)
    implementation(vcl.androidx.lifecycle.runtime.compose)
    implementation(vcl.androidx.lifecycle.viewmodel.compose)
    implementation(vcl.androidx.lifecycle.viewmodel.navigation3)

    // --- View UI ---
    implementation(vcl.androidx.appcompat)
    implementation(vcl.androidx.viewpager2)
    implementation(vcl.androidx.recyclerview)
    implementation(vcl.androidx.constraintlayout)
    implementation(vcl.androidx.swiperefreshlayout)
    implementation(vcl.androidx.dynamicanimation)
    implementation(vcl.androidx.dynamicanimation.ktx)
    implementation(vcl.androidx.webkit)

    // --- Room / DataStore / Paging ---
    implementation(vcl.androidx.room.common)
    implementation(vcl.androidx.room.runtime)
    implementation(vcl.androidx.room.ktx)
    implementation(vcl.androidx.room.paging)
    ksp(vcl.androidx.room.compiler)
    implementation(vcl.androidx.datastore)
    implementation(vcl.androidx.datastore.preferences)
    implementation(vcl.androidx.paging.common.ktx)
    implementation(vcl.androidx.paging.runtime.ktx)
    implementation(vcl.androidx.paging.runtime)
    implementation(vcl.androidx.paging.compose)

    // --- Compose BOM ---
    implementation(platform(vcl.androidx.compose.bom))
    implementation(vcl.androidx.compose.ui)
    implementation(vcl.androidx.compose.ui.util)
    implementation(vcl.androidx.compose.ui.unit)
    implementation(vcl.androidx.compose.ui.text)
    implementation(vcl.androidx.compose.ui.graphics)
    implementation(vcl.androidx.compose.ui.geometry)
    implementation(vcl.androidx.compose.runtime)
    implementation(vcl.androidx.compose.foundation)
    implementation(vcl.androidx.compose.foundation.layout)
    implementation(vcl.androidx.compose.runtime.livedata)
    implementation(vcl.androidx.compose.animation)
    implementation(vcl.androidx.compose.animation.core)
    implementation(vcl.androidx.compose.animation.graphics)
    implementation(vcl.androidx.compose.material.icons.core)
    implementation(vcl.androidx.compose.material.icons.extended)
    implementation(vcl.androidx.compose.material3)
    implementation(vcl.androidx.compose.material3.window.size)
    implementation(vcl.androidx.compose.ui.viewbinding)
    debugImplementation(vcl.androidx.compose.ui.tooling)
    debugImplementation(vcl.androidx.compose.ui.tooling.data)
    implementation(vcl.androidx.compose.ui.tooling.preview)
    androidTestImplementation(vcl.androidx.compose.ui.test.junit4)
    debugImplementation(vcl.androidx.compose.ui.test.manifest)
    implementation(vcl.androidx.compose.activity)
    implementation(vcl.androidx.compose.constraintlayout)
    implementation(vcl.androidx.compose.ui.text.google.fonts)
    implementation(vcl.androidx.compose.material3.adaptive.navigation.suite)
    implementation(vcl.androidx.compose.material3.adaptive.navigation)
    implementation(vcl.androidx.navigation3.runtime)
    implementation(vcl.androidx.navigation3.ui)
    implementation(vcl.androidx.graphics.shapes)
    implementation(vcl.androidx.graphics.path)
    implementation(vcl.androidx.graphics.core)

    // --- Media3 / Glance ---
    implementation(vcl.androidx.media3.exoplayer)
    implementation(vcl.androidx.media3.common)
    implementation(vcl.androidx.media3.ui)
    implementation(vcl.androidx.media3.ui.compose.material3)
    implementation(vcl.androidx.glance.appwidget)
    implementation(vcl.androidx.glance.material3)

    // --- Google / Accompanist / ML ---
    implementation(vcl.google.material)
    implementation(vcl.google.auto.service.anno)
    implementation(vcl.google.dagger.hilt.core)
    implementation(vcl.google.dagger.hilt.android)
    ksp(vcl.google.dagger.hilt.compiler)
    implementation(vcl.protobuf.kotlin.lite)
    implementation(vcl.protobuf.javalite)
    implementation(vcl.mediapipe.vision)
    implementation(vcl.mlkit.segmentation)
    implementation(vcl.google.accompanist.drawablepainter)
    implementation(vcl.google.accompanist.flowlayout)
    implementation(vcl.google.accompanist.insets)
    implementation(vcl.google.accompanist.pager)
    implementation(vcl.google.accompanist.pager.indicators)
    implementation(vcl.google.accompanist.permissions)
    implementation(vcl.google.accompanist.placeholder.material)
    implementation(vcl.google.accompanist.swiperefresh)
    implementation(vcl.google.accompanist.systemuicontroller)
    implementation(vcl.google.accompanist.webview)

    // --- Coil / KMP AndroidX ---
    implementation(vcl.kmp.coil.compose)
    implementation(vcl.kmp.coil.compose.android)
    implementation(vcl.kmp.androidx.paging)
    implementation(vcl.kmp.androidx.annotation)
    implementation(vcl.kmp.androidx.collection.ktx)
    implementation(vcl.kmp.androidx.datastore)
    implementation(vcl.kmp.androidx.lifecycle.process)
    implementation(vcl.kmp.androidx.lifecycle.runtime.ktx)
    implementation(vcl.kmp.androidx.lifecycle.livedata.ktx)
    implementation(vcl.kmp.androidx.lifecycle.viewmodel.ktx)
    implementation(vcl.kmp.androidx.lifecycle.runtime.compose)
    implementation(vcl.kmp.androidx.lifecycle.viewmodel.compose)
    implementation(vcl.kmp.androidx.lifecycle.viewmodel.savedstate)

    // --- OkHttp / Retrofit ---
    implementation(platform(vcl.okhttp.bom))
    implementation(vcl.okhttp)
    implementation(vcl.okhttp.logging)
    implementation(vcl.retrofit)
    implementation(vcl.retrofit.kotlinx.serialization.converter)

    // --- Ktor ---
    implementation(platform(vcl.ktor.bom))
    implementation(vcl.ktor.client)
    implementation(vcl.ktor.client.logging)
    implementation(vcl.ktor.client.content.negotiation)
    implementation(vcl.ktor.serialization.kotlinx.json)
    implementation(vcl.ktor.client.auth)
    implementation(vcl.ktor.client.resources)
    implementation(vcl.ktor.client.encoding)
    implementation(vcl.ktor.client.websockets)
    implementation(vcl.ktor.client.okhttp)
    implementation(vcl.ktor.client.cio)

    // --- Koin ---
    implementation(platform(vcl.koin.bom))
    implementation(vcl.koin.core)
    testImplementation(vcl.koin.test)
    implementation(vcl.koin.core.coroutines)
    implementation(vcl.koin.android)
    implementation(vcl.koin.android.compat)
    androidTestImplementation(vcl.koin.android.test)
    implementation(vcl.koin.androidx.navigation)
    implementation(vcl.koin.androidx.workmanager)
    implementation(vcl.koin.androidx.compose)
    implementation(vcl.koin.compose)
    implementation(vcl.koin.androidx.compose.navigation)
    implementation(vcl.koin.ktor)

    // --- Kotlin / KotlinX / Lottie ---
    implementation(vcl.kotlinx.coroutines.core)
    implementation(vcl.kotlinx.coroutines.core.jvm)
    implementation(vcl.kotlinx.coroutines.android)
    implementation(vcl.kotlinx.serialization.json)
    implementation(vcl.kotlinx.datetime)
    implementation(vcl.kotlinx.atomicfu)
    implementation(vcl.kotlinx.io)
    implementation(vcl.lottie)
    implementation(vcl.lottie.compose)
    implementation(vcl.ksp.process.api)
    implementation(vcl.ksp.poe)
    testImplementation(vcl.test.junit)
    testImplementation(vcl.test.mockk)
    implementation(vcl.kotlin.reflect)

    // --- Android Test / Benchmark ---
    androidTestImplementation(vcl.androidx.test.ext.junit)
    androidTestImplementation(vcl.androidx.test.espresso.core)
    androidTestImplementation(vcl.androidx.test.uiautomator)
    androidTestImplementation(vcl.androidx.benchmark.macro.junit4)

    implementation("pub.devrel:easypermissions:3.0.0")
}
