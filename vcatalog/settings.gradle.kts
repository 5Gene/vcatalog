import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
            fun beijingTimeVersion(): String {
                val beijingTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
                val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                return beijingTime.format(formatter)
            }
//            version("gene-vcl", beijingTimeVersion())
        }
    }
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}