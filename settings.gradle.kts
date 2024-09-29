

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        //https://github.com/jamesward/kotlin-universe-catalog
        // versionCatalogs可以用依赖
        //  versionCatalogs {
        //        create("universe") {
        //            from("com.jamesward.kotlin-universe-catalog:stables:2024.06.12-2")
        //        }
        //    }
        create("wings") {
            from(files("gradle/libs.versions.toml"))
        }
    }
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "vcatalog"
