// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(vcl.plugins.android.application) apply false
    alias(vcl.plugins.kotlin.android) apply false
    alias(vcl.plugins.kotlin.jvm) apply false
}

buildscript {
    dependencies {
        classpath("io.github.5hmlA:conventions:${vcl.versions.gene.conventions.get()}")
    }
}