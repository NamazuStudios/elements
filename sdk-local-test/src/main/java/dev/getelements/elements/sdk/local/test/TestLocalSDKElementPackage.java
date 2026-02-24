package dev.getelements.elements.sdk.local.test;

import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import static dev.getelements.elements.sdk.deployment.ElementContainerService.APPLICATION_PREFIX;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_WS;

public class TestLocalSDKElementPackage extends AbstractTestLocalSDK {

    private static final Logger logger = LoggerFactory.getLogger(TestLocalSDKElementPackage.class);

    private final SharedLocalSDK shared = SharedLocalSDK.getInstance();

    @Override
    protected String appPath() {
        return "myapp_maven";
    }

    @BeforeClass
    public void setUpLocalRunner() throws InterruptedException {

        final var deployment = TransientDeploymentRequest.builder()
                .useDefaultRepositories(true)
                .elementPackage()
                    .elmArtifact(JAKARTA_RS.getCoordinatesForElm())
                    .pathAttribute(
                            "dev.getelements.elements.sdk-test-element-rs",
                            APPLICATION_PREFIX,
                            appPath()
                    )
                .endElementPackage()
                .elementPackage()
                    .elmArtifact(JAKARTA_WS.getCoordinatesForElm())
                    .pathAttribute(
                            "dev.getelements.elements.sdk-test-element-ws",
                            APPLICATION_PREFIX,
                            appPath()
                    )
                .endElementPackage()
                .build();

        final var result = shared.getElementsLocal().getRuntimeService().loadTransientDeployment(deployment);
        logger.info("Loaded deployment {}", result.deployment().id());

    }

}