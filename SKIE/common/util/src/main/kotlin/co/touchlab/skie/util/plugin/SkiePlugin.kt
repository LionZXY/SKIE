package co.touchlab.skie.util.plugin

import co.touchlab.skie.util.directory.SkieDirectories
import java.io.File

object SkiePlugin {

    const val id = "co.touchlab.skie"

    object Options {

        val skieDirectories = Option(
            optionName = "skieBuildDirectory",
            valueDescription = "<absolute path>",
            description = "",
            isRequired = true,
            serialize = { it.buildDirectory.directory.absolutePath },
            deserialize = { SkieDirectories(File(it)) },
        )

        // One occurrence per external (non-project) dependency klib of the framework, encoded as
        // "<unique name>|<version>|<absolute path>". Used to distinguish external libraries from local modules on
        // Kotlin 2.4.0+, where the compiler no longer exposes this information (UserVisibleIrModulesSupport was removed).
        val externalDependency = Option(
            optionName = "externalDependency",
            valueDescription = "<unique name>|<version>|<absolute path>",
            description = "",
            allowMultipleOccurrences = true,
            serialize = { it },
            deserialize = { it },
        )
    }

    data class Option<T>(
        val optionName: String,
        val valueDescription: String,
        val description: String,
        val isRequired: Boolean = false,
        val allowMultipleOccurrences: Boolean = false,
        val serialize: (T) -> String,
        val deserialize: (String) -> T,
    )
}
