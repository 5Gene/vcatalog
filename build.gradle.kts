import june.tasks.UpdateVersionCatalogTask
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

// 其他项目中 AS 中搜索 Newer Library Versions Available 关闭 AS 自动检测，否则AS会自动检测toml中的版本号更新
//todo 执行这个task 自动更新 wings.versions.toml 中需要升级的版本号
tasks.register<UpdateVersionCatalogTask>(
    "updateVersionCatalog"
) {
    tomlFile.set(
        rootProject.layout.projectDirectory.file(
            "wings.versions.toml"
//            "gradle/libs.versions.toml"
        )
    )
}
