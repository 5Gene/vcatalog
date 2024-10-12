import wing.publishMavenCentral

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    `version-catalog`
    `maven-publish`
}
buildscript {
    dependencies {
//        classpath(libs.gene.conventions)
        classpath("io.github.5hmlA:conventions:2.1.10")
    }
}

catalog {
    versionCatalog {
        from(files("gradle/libs.versions.toml"))
    }
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}

group = "io.github.5hmla"
version = libs.versions.gene.vcl.get()

publishMavenCentral("version catalog", "versionCatalog", false)