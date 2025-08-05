import june.wing.publishMavenCentral

buildscript {
    dependencies {
        classpath(libs.gene.conventions)
    }
}

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    //    id("java-library")
    `version-catalog`
    `maven-publish`
}

//java {
//    sourceCompatibility = JavaVersion.VERSION_17
//    targetCompatibility = JavaVersion.VERSION_17
//}

catalog {
    versionCatalog {
        from(files("wings.versions.toml"))
    }
}

publishing {
    publications {
        create<MavenPublication>("vcatalog") {
            from(components["versionCatalog"])
        }
    }
}

group = "io.github.5hmla"
version = libs.versions.gene.vcl.get()

publishMavenCentral("version catalog", "versionCatalog", false)
