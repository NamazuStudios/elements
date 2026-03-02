#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
import dev.getelements.elements.sdk.local.ElementsLocalBuilder;

/**
 * Runs your local Element in the SDK.
 */
public class run {
    public static void main(final String[] args ) throws Exception {

        new ProcessBuilder("docker", "compose", "up", "-d")
                .directory(new java.io.File("services-dev"))
                .inheritIO()
                .start()
                .waitFor();

        final var local = ElementsLocalBuilder.getDefault()
                .withSourceRoot()
                .withDeployment(builder -> builder
                        .useDefaultRepositories(true)
                        .elementPath()
                            .addSpiBuiltin("DEFAULT")
                            .addApiArtifact("${groupId}:api:${version}")
                            .addElementArtifact("${groupId}:element:${version}")
                        .endElementPath()
                        .build()
                )
                .build();

        local.start();
        local.run();

    }
}
