@file:JvmName("Run")

import dev.getelements.elements.sdk.local.ElementsLocalBuilder
import java.io.File

/**
 * Runs your local Element in the SDK.
 *
 * Working directory must be the project root.
 * IntelliJ: Run → Edit Configurations → Working directory → set to this project root.
 */
fun main() {

    // Install npm dependencies on first run, then build both segment bundles.
    // The bundles are written directly to element/src/main/ui/{superuser,user}/
    // so that the Maven build triggered by local.start() picks them up.
    val uiDir = File("ui")

    if (!File(uiDir, "node_modules").exists()) {
        ProcessBuilder("npm", "install")
            .directory(uiDir)
            .inheritIO()
            .start()
            .waitFor()
    }

    ProcessBuilder("npm", "run", "build")
        .directory(uiDir)
        .inheritIO()
        .start()
        .waitFor()

    ProcessBuilder("docker", "compose", "up", "-d")
        .directory(File("services-dev"))
        .inheritIO()
        .start()
        .waitFor()

    val local = ElementsLocalBuilder.getDefault()
        .withSourceRoot()
        .withDeployment { builder ->
            builder
                .useDefaultRepositories(true)
                .elementPackage()
                    .elmArtifact("${groupId}:element:elm:${version}")
                .endElementPackage()
                .build()
        }
        .build()

    local.start()
    local.run()

}
