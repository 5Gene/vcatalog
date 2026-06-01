pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("vcl") {
            from(files("../wings.versions.toml"))
            // 覆盖 TOML 文件中声明的 版本
            version("gene-conventions", "2026.06.01")
        }
    }
}

rootProject.name = "check"
include(":lib")
include(":app")
