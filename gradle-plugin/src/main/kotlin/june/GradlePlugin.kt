package june

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.lang.IllegalStateException
import java.util.Properties

class GradlePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.gradle.settingsEvaluated {
            settings.dependencyResolutionManagement {
                versionCatalogs {
                    create("vcl") {
                        from("io.github.5hmla:vcatalog:24.10.01")
                    }
                }
            }
        }
    }
}
