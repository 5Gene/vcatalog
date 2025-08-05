import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.api.model.ObjectFactory

// ServerConfig.kt
open class ServerConfig(val name: String) {
    var url: String = ""
}

//myPlugin {
//    servers {
//        create("dev") {
//            url = "https://dev.example.com"
//        }
//        create("prod") {
//            url = "https://prod.example.com"
//        }
//    }
//}

//myPlugin {
//    servers {
//        dev {
//            url = "https://dev.example.com"
//        }
//        prod {
//            url = "https://prod.example.com"
//        }
//    }
//}

// MyPluginExtension.kt
open class MyPluginExtension(objects: ObjectFactory) {
    val servers: NamedDomainObjectContainer<ServerConfig> =
        objects.domainObjectContainer(ServerConfig::class.java)
}

class MyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("🛠 插件已应用在: ${project.name}")
        val extension = project.extensions.create(
            "myPlugin",
            MyPluginExtension::class.java,
            project.objects
        )

        // 示例：在项目评估后打印所有配置
        project.afterEvaluate {
            extension.servers.forEach {
                println("Server '${it.name}': ${it.url}")
            }
        }
    }
}