@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.compat.KonanConfig
import co.touchlab.skie.compilerinject.compilerplugin.SkieConfigurationKeys
import org.jetbrains.kotlin.utils.ResolvedDependency
import org.jetbrains.kotlin.utils.ResolvedDependencyArtifactPath
import org.jetbrains.kotlin.utils.ResolvedDependencyId
import org.jetbrains.kotlin.utils.ResolvedDependencyVersion

/**
 * Kotlin 2.4.0 removed `UserVisibleIrModulesSupport`, which previously exposed the external (3rd-party) dependency
 * modules. Instead, the SKIE Gradle plugin resolves the framework's link dependencies and passes the external
 * (non-project) klibs via the `externalDependency` compiler-plugin option, encoded as
 * "<unique name>|<version>|<absolute path>" (one occurrence per artifact).
 *
 * This reconstructs the [ResolvedDependency] set from that data so the external-vs-local library distinction (used by
 * default-argument interop and the module analytics) keeps working as it did before 2.4.0.
 */
internal fun getExternalDependencies(konanConfig: KonanConfig): Set<ResolvedDependency> {
    val encodedArtifacts = konanConfig.configuration.get(SkieConfigurationKeys.ExternalDependencies).orEmpty()

    return encodedArtifacts
        .mapNotNull { encoded ->
            val parts = encoded.split("|", limit = 3)

            if (parts.size == 3) Triple(parts[0], parts[1], parts[2]) else null
        }
        .groupBy(keySelector = { it.first to it.second }, valueTransform = { it.third })
        .map { (uniqueNameAndVersion, paths) ->
            val (uniqueName, version) = uniqueNameAndVersion

            ResolvedDependency(
                id = ResolvedDependencyId(uniqueName),
                selectedVersion = ResolvedDependencyVersion(version),
                requestedVersionsByIncomingDependencies = mutableMapOf(),
                artifactPaths = paths.mapTo(mutableSetOf()) { ResolvedDependencyArtifactPath(it) },
            )
        }
        .toSet()
}
