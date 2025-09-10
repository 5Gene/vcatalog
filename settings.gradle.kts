import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

pluginManagement {
    //Gradle的复合构建（composite builds）功能
    includeBuild("vcatalog")
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
            fun beijingTimeVersion(): String {
                val beijingTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
                val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                return beijingTime.format(formatter)
            }
            // overwrite the "groovy" version declared in the imported catalog
            version("gene-vcl", beijingTimeVersion())
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

//plugins {
//    id("io.github.5hmlA.vcl")
//}

includeBuild("check")
rootProject.name = "vcatalog"
