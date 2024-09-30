
plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish")
}

group = "io.github.5hmlA"
version = "24.09.30"


gradlePlugin {
    website = "https://github.com/5Gene/vcatalog"
    vcsUrl = "https://github.com/5Gene/vcatalog"
    plugins {
        register("vcatalog") {
            id = "${group}.vc"
            displayName = "gracle version catalog"
            description = "gracle version catalog"
            tags = listOf("config", "versionCatalog", "convention")
            implementationClass = "june.GradlePlugin"
        }
    }
}

tasks.getByName("publishPlugins").doLast {
    println("插件发布成功，点击🔗查看：https://plugins.gradle.org/")
}