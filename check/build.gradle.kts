import org.gradle.kotlin.dsl.vcl

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(vcl.plugins.android.application) apply false
    alias(vcl.plugins.kotlin.jvm) apply false
    alias(vcl.plugins.kotlin.serialization) apply false
    alias(vcl.plugins.gene.compose) apply false
    alias(vcl.plugins.gene.android) apply false
    alias(vcl.plugins.compose.compiler) apply false
}

buildscript {
    dependencies {
        classpath("io.github.5hmlA:conventions:${vcl.versions.gene.conventions.get()}")
    }
}