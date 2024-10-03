import wing.publish5hmlA
import wing.publishJava5hmlA
import wing.publishMavenCentral

plugins {
    `version-catalog`
    `maven-publish`
}
buildscript {
    dependencies {
        classpath(wings.gene.conventions)
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
version = "24.10.01"

publishMavenCentral("version catalog", "versionCatalog", false)