package june

import io.github._5hmlA.vcatalog.BuildConfig
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.extra
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//vcl {
//    onCreate {
//        version("google-ksp", "2.2.0-2.0.2")  //gradle.kts
//        it.version("google-ksp", "2.2.0-2.0.2") //gradle
//    }
//    doOverride {
//        version("google-ksp", "2.2.0-2.0.2")  //gradle.kts
//        version("google-ksp", "2.2.0-2.0.2") //gradle
//    }
//}
interface VclOverride {
    fun onCreate(callback: VersionCatalogBuilder.() -> Unit)

    fun doOverride(callback: Action<VersionCatalogBuilder>)
}

open class VclOverrideImpl : VclOverride {
    var onCreate: (VersionCatalogBuilder.() -> Unit)? = null
    var overrideAction: Action<VersionCatalogBuilder>? = null

    override fun onCreate(callback: (VersionCatalogBuilder) -> Unit) {
        onCreate = callback
    }

    override fun doOverride(callback: Action<VersionCatalogBuilder>) {
        overrideAction = callback
    }
}


abstract class VclOverrideExtension(private val vclOverrideImpl: VclOverrideImpl) : VclOverride, ExtensionAware {

    override fun onCreate(callback: VersionCatalogBuilder.() -> Unit) {
        vclOverrideImpl.onCreate(callback)
    }

    override fun doOverride(callback: Action<VersionCatalogBuilder>) {
        vclOverrideImpl.doOverride(callback)
    }
}

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
        val vclOverrideImpl = VclOverrideImpl()
        settings.extensions.create("vcl",VclOverrideExtension::class.java, vclOverrideImpl)
        settings.gradle.settingsEvaluated {
            repositoryConfig(settings)
            settings.dependencyResolutionManagement {
                versionCatalogs {
                    create("vcl") {
                        from("io.github.5hmla:vcatalog:${BuildConfig.VCL_VERSION}")
                        vclOverrideImpl.onCreate?.invoke(this@create)
                        vclOverrideImpl.overrideAction?.execute(this)
                        // https://docs.gradle.org/current/userguide/version_catalogs.html
                        // overwrite the "groovy" version declared in the imported catalog
                        // version("groovy", "3.0.6")
                    }
                }
            }

        }
    }

    private fun Settings.repositoryConfig(settings: Settings) {
        val repositoriesMode = dependencyResolutionManagement.repositoriesMode.get()
        println("vcl => settingsEvaluated -> gradle:${settings.gradle.hashCode()} -> repositoriesMode: $repositoriesMode")
        settings.gradle.extra["repositoriesMode"] = repositoriesMode
        if (repositoriesMode == RepositoriesMode.PREFER_SETTINGS || repositoriesMode == RepositoriesMode.FAIL_ON_PROJECT_REPOS) {
            settings.dependencyResolutionManagement.repositories.addRepositoryFirst {
                maven {
                    name = "tencent_vcl"
                    // 用于指定是否允许通过不安全的协议（如HTTP）与仓库进行通信。默认情况下，
                    // Gradle 会优先使用安全的协议（如HTTPS）来确保数据传输的安全性。
                    // 但是，在某些情况下，你可能需要与仅支持HTTP的仓库进行通信，
                    // 这时就需要配置 isAllowInsecureProtocol 来允许这种不安全的连接
                    //isAllowInsecureProtocol = true
                    setUrl("https://mirrors.tencent.com/nexus/repository/maven-public/")
                    content {
                        //https://blog.csdn.net/jklwan/article/details/99351808
                        excludeGroupByRegex("osp.spark.*")
                        excludeGroupByRegex("osp.june.*")
                        excludeGroupByRegex("osp.gene.*")
                        excludeGroup("aar")
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
            println("vcl => settingsEvaluated -> repositories -> ${it.name} ====== $it")
        }
    }
}


val isCI: Boolean by lazy {
    System.getenv("CI") == "true" || System.getenv("GITHUB_ACTIONS") == "true" || System.getenv("JENKINS_HOME") != null
}

fun beijingTimeVersion(): String {
    val beijingTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return beijingTime.format(formatter)
}