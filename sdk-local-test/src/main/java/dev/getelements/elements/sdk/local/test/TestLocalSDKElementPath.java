package dev.getelements.elements.sdk.local.test;

import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import org.testng.annotations.BeforeClass;

import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_WS;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;

public class TestLocalSDKElementPath extends AbstractTestLocalSDK {

    private final SharedLocalSDK shared = SharedLocalSDK.getInstance();

    @Override
    protected String appPath() {
        return "myapp";
    }

    @BeforeClass
    public void setUpLocalRunner() {

        final var deployment = TransientDeploymentRequest.builder()
                .useDefaultRepositories(true)
                .elementPath()
                    .path("rs")
                    .addSpiArtifacts(GUICE_7_0_X.getAllCoordinates().toList())
                    .addElementArtifacts(JAKARTA_RS.getAllCoordinates().toList())
                .endElementPath()
                .elementPath()
                    .path("ws")
                    .addSpiArtifacts(GUICE_7_0_X.getAllCoordinates().toList())
                    .addElementArtifacts(JAKARTA_WS.getAllCoordinates().toList())
                .endElementPath()
                .build();

        shared.getElementsLocal().getRuntimeService().loadTransientDeployment(deployment);

    }

}