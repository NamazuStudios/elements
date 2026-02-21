package dev.getelements.elements.sdk.local.test;

import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import org.testng.annotations.BeforeClass;

import static dev.getelements.elements.sdk.deployment.ElementContainerService.APPLICATION_PREFIX;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_WS;

public class TestLocalSDKElementPackage extends AbstractTestLocalSDK {

    private final SharedLocalSDK shared = SharedLocalSDK.getInstance();

    @Override
    protected String appPath() {
        return "myapp_maven";
    }

    @BeforeClass
    public void setUpLocalRunner() {

        final var deployment = TransientDeploymentRequest.builder()
                .useDefaultRepositories(true)
                .elementPackage()
                    .addPathSpiBuiltin("dev.getelements.elements.sdk-test-element-rs", "DEFAULT")
                    .elmArtifact(JAKARTA_RS.getCoordinatesForElm())
                    .pathAttribute(
                            "dev.getelements.elements.sdk-test-element-rs",
                            APPLICATION_PREFIX,
                            "/myapp_maven"
                    )
                .endElementPackage()
                .elementPackage()
                    .addPathSpiBuiltin("dev.getelements.elements.sdk-test-element-ws", "DEFAULT")
                    .elmArtifact(JAKARTA_WS.getCoordinatesForElm())
                    .pathAttribute(
                            "dev.getelements.elements.sdk-test-element-ws",
                            APPLICATION_PREFIX,
                            "/myapp_maven"
                    )
                .endElementPackage()
                .build();

        shared.getElementsLocal().getRuntimeService().loadTransientDeployment(deployment);

    }

}