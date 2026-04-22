#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
import dev.getelements.elements.sdk.local.ElementsLocalBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Runs your local Element in the SDK.
 *
 * Working directory must be the project root.
 * IntelliJ: Run → Edit Configurations → Working directory → set to this project root.
 */
public class run {
    public static void main(final String[] args ) throws IOException, InterruptedException {

        // Install npm dependencies on first run, then build both segment bundles.
        // The bundles are written directly to element/src/main/ui/{superuser,user}/
        // so that the Maven build triggered by local.start() picks them up.
        final var uiDir = new File("ui");

        if (!new File(uiDir, "node_modules").exists()) {
            new ProcessBuilder("npm", "install")
                    .directory(uiDir)
                    .inheritIO()
                    .start()
                    .waitFor();
        }

        new ProcessBuilder("npm", "run", "build")
                .directory(uiDir)
                .inheritIO()
                .start()
                .waitFor();

        new ProcessBuilder("docker", "compose", "up", "-d")
                .directory(new File("services-dev"))
                .inheritIO()
                .start()
                .waitFor();

        final var local = ElementsLocalBuilder.getDefault()
                .withSourceRoot()
                .withDeployment(builder -> builder
                        .useDefaultRepositories(true)
                        .elementPackage()
                        .elmArtifact("${groupId}:element:elm:${version}")
                        .endElementPackage()
                        .build()
                )
                .build();

        local.start();
        local.run();

    }

}
