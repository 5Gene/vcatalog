// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(plugins.plugins.android.application) apply false
    alias(plugins.plugins.kotlin.android) apply false
    alias(plugins.plugins.kotlin.jvm) apply false
}

buildscript {
    dependencies {
        classpath("io.github.5hmlA:conventions:${gene.versions.gene.conventions.get()}")
    }
}