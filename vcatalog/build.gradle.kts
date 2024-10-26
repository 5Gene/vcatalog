import june.wing.publishGradlePluginSet
import org.gradle.kotlin.dsl.libs

buildscript {
    dependencies {
        classpath(libs.gene.conventions)
    }
}

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.build.config)
}

//https://github.com/gmazzo/gradle-buildconfig-plugin
buildConfig {
    buildConfigField("VCL_VERSION", provider { "${project.version}" })
}

group = "io.github.5hmlA"
version = libs.versions.gene.vcl.get()

publishGradlePluginSet(false) {
    register("plugin-vcl") {
        id = "${group}.vcl"
        displayName = "gracle version catalog"
        description = "gracle version catalog"
        tags = listOf("config", "versionCatalog", "convention")
        implementationClass = "june.VCatalogPlugin"
    }
}

tasks.findByName("publishPlugins")?.doLast {
    println("插件发布成功，点击🔗查看：https://plugins.gradle.org/")
}