#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
import dev.getelements.elements.sdk.local.ElementsLocalBuilder;

/**
 * Runs your local Element in the SDK.
 */
public class run {
    public static void main(final String[] args ) {

        final var local = ElementsLocalBuilder.getDefault()
                .withSourceRoot()
                .withDeployment(builder -> builder
                        .useDefaultRepositories(true)
                        .elementPath()
                            .addSpiBuiltin("GUICE_7_0_0")
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
