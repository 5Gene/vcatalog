import june.wing.publishMavenCentral
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

buildscript {
    dependencies {
        classpath(libs.gene.conventions)
    }
}

plugins {
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

fun beijingTimeVersion(): String {
    val beijingTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return beijingTime.format(formatter)
}

group = "io.github.5hmla"
version = beijingTimeVersion()

publishMavenCentral("version catalog", "versionCatalog", false)
