package co.touchlab.skie.plugin.dependencies

import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.util.toKotlinCompilerPluginOption
import co.touchlab.skie.util.file.isKlib
import co.touchlab.skie.util.plugin.SkiePlugin
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

/**
 * Kotlin 2.4.0 removed the compiler API SKIE used to tell external (3rd-party) dependencies apart from local project
 * modules (`UserVisibleIrModulesSupport`). Gradle knows this distinction, so SKIE resolves the framework's link
 * dependencies and forwards the external (non-project) klibs to the compiler plugin via the `externalDependency` option.
 *
 * The data is consumed only on Kotlin 2.4.0+ (see `getExternalDependencies` in the `2.4.0..` source set); on older
 * versions the compiler still provides it directly, so the forwarded values are ignored.
 */
fun SkieTarget.passExternalDependenciesToCompiler() {
    linkerConfiguration.incoming.afterResolve {
        linkerConfiguration.resolvedConfiguration.resolvedArtifacts
            .filter { it.id.componentIdentifier is ModuleComponentIdentifier }
            .filter { it.file.isKlib }
            .forEach { artifact ->
                val moduleVersion = artifact.moduleVersion.id
                val encoded = "${moduleVersion.module}|${moduleVersion.version}|${artifact.file.absolutePath}"

                addPluginArgument(
                    SkiePlugin.id,
                    SkiePlugin.Options.externalDependency.toKotlinCompilerPluginOption(encoded),
                )
            }
    }
}
