package june

import io.github._5hmlA.vcatalog.BuildConfig
import org.gradle.api.Plugin
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.kotlin.dsl.extra

class VCatalogPlugin : Plugin<Settings> {
    fun RepositoryHandler.addRepositoryFirst(addRepoAction: RepositoryHandler.() -> Unit) {
        addRepoAction()
        //拿到所有仓库
        //val repositories = this.toList()
        if (size > 1) {
            val removeLast = removeLast()
            addFirst(removeLast)
        }
    }

    override fun apply(settings: Settings) {
        settings.gradle.settingsEvaluated {
            repositoryConfig(settings)
            settings.dependencyResolutionManagement {
                versionCatalogs {
                    create("vcl") {
                        from("io.github.5hmla:vcatalog:${BuildConfig.VCL_VERSION}")
                    }
                }
            }
        }
    }

    private fun Settings.repositoryConfig(settings: Settings) {
        val repositoriesMode = dependencyResolutionManagement.repositoriesMode.get()
        println("settingsEvaluated -> gradle:${settings.gradle.hashCode()} -> repositoriesMode: $repositoriesMode")
        settings.gradle.extra["repositoriesMode"] = repositoriesMode
        if (repositoriesMode == RepositoriesMode.PREFER_SETTINGS || repositoriesMode == RepositoriesMode.FAIL_ON_PROJECT_REPOS) {
            settings.dependencyResolutionManagement.repositories.addRepositoryFirst {
                maven {
                    name = "tencent"
                    isAllowInsecureProtocol = true
                    setUrl("https://mirrors.tencent.com/nexus/repository/maven-public/")
                    content {
                        //https://blog.csdn.net/jklwan/article/details/99351808
                        excludeGroupByRegex("osp.spark.*")
                        excludeGroupByRegex("osp.june.*")
                        excludeGroupByRegex("osp.gene.*")
                    }
                }
            }
        }

        settings.dependencyResolutionManagement.repositories.forEach {
            if (it.name == "Google") {
                it.content {
                    includeGroupByRegex("com\\.android.*")
                    includeGroupByRegex("com\\.google.*")
                    includeGroupByRegex("androidx.*")
                }
            }
            println("settingsEvaluated -> repositories -> ${it.name} ====== $it")
        }
    }
}


val isCI: Boolean by lazy {
    System.getenv("CI") == "true" || System.getenv("GITHUB_ACTIONS") == "true" || System.getenv("JENKINS_HOME") != null
}