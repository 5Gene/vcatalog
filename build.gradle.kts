import wing.publishMavenCentral

plugins {
    `version-catalog`
    `maven-publish`
}
buildscript {
    dependencies {
        classpath(libs.gene.conventions)
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
version = libs.versions.vcl.get()

publishMavenCentral("version catalog", "versionCatalog", false)