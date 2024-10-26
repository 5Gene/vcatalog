package june

import io.github._5hmlA.vcatalog.BuildConfig
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class VCatalogPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        settings.gradle.settingsEvaluated {
            settings.dependencyResolutionManagement {
                versionCatalogs {
                    create("vcl") {
                        from("io.github.5hmla:vcatalog:${BuildConfig.VCL_VERSION}")
                    }
                }
            }
        }
    }

}
