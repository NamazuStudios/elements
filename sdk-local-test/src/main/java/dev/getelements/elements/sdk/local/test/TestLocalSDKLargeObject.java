package dev.getelements.elements.sdk.local.test;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import dev.getelements.elements.sdk.model.largeobject.AccessPermissions;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.Subjects;
import dev.getelements.elements.sdk.record.Artifact;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.util.List;

import static dev.getelements.elements.sdk.deployment.ElementContainerService.APPLICATION_PREFIX;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_WS;

public class TestLocalSDKLargeObject extends AbstractTestLocalSDK {

    private static final Logger logger = LoggerFactory.getLogger(TestLocalSDKLargeObject.class);

    private final SharedLocalSDK shared = SharedLocalSDK.getInstance();

    @Override
    protected String appPath() {
        return "myapp_large_object";
    }

    @BeforeClass
    public void setUpLocalRunner() throws InterruptedException, IOException {

        final var largeObjectDao = shared
                .getElementsLocal()
                .getRootElementRegistry()
                .find("dev.getelements.elements.sdk.dao")
                .findAny()
                .orElseThrow(IllegalStateException::new)
                .getServiceLocator()
                .getInstance(LargeObjectDao.class);

        final var largeObjectBucket = shared
                .getElementsLocal()
                .getRootElementRegistry()
                .find("dev.getelements.elements.sdk.dao")
                .findAny()
                .orElseThrow(IllegalStateException::new)
                .getServiceLocator()
                .getInstance(LargeObjectBucket.class);

        final var nobody = new AccessPermissions();
        nobody.setRead(Subjects.nobody());
        nobody.setWrite(Subjects.nobody());
        nobody.setDelete(Subjects.nobody());

        var rsLargeObject = new LargeObject();
        rsLargeObject.setPath("/element/test/deployment/rs.elm");
        rsLargeObject.setMimeType(ElementPathLoader.ELM_MIME_TYPE);
        rsLargeObject.setAccessPermissions(nobody);

        var wsLargeObject = new LargeObject();
        wsLargeObject.setPath("/element/test/deployment/ws.elm");
        wsLargeObject.setMimeType(ElementPathLoader.ELM_MIME_TYPE);
        wsLargeObject.setAccessPermissions(nobody);

        rsLargeObject = largeObjectDao.createLargeObject(rsLargeObject);
        wsLargeObject = largeObjectDao.createLargeObject(wsLargeObject);

        final var artifactLoader = ElementArtifactLoader.newDefaultInstance();

        try (final var os = largeObjectBucket.writeObject(rsLargeObject.getId());
             final var is = artifactLoader.findArtifact(ArtifactRepository.DEFAULTS, JAKARTA_RS.getCoordinatesForElm())
                                          .map(Artifact::read)
                                          .orElseThrow(IllegalStateException::new)) {
            is.transferTo(os);
        }

        try (final var os = largeObjectBucket.writeObject(wsLargeObject.getId());
             final var is = artifactLoader.findArtifact(ArtifactRepository.DEFAULTS, JAKARTA_WS.getCoordinatesForElm())
                     .map(Artifact::read)
                     .orElseThrow(IllegalStateException::new)) {
            is.transferTo(os);
        }

        final var rsDeployment = TransientDeploymentRequest.builder()
                .useDefaultRepositories(true)
                .elmLargeObjectId(rsLargeObject.getId())
                .addPathAttribute(
                        "dev.getelements.elements.sdk-test-element-rs",
                        APPLICATION_PREFIX,
                        appPath()
                )
                .addPathSpiBuiltins("dev.getelements.elements.sdk-test-element-rs", List.of("DEFAULT"))
                .build();

        final var wsDeployment = TransientDeploymentRequest.builder()
                .useDefaultRepositories(true)
                .elmLargeObjectId(wsLargeObject.getId())
                .addPathAttribute(
                        "dev.getelements.elements.sdk-test-element-ws",
                        APPLICATION_PREFIX,
                        appPath()
                )
                .addPathSpiBuiltins("dev.getelements.elements.sdk-test-element-ws", List.of("DEFAULT"))
                .build();

        final var rsResult = shared.getElementsLocal().getRuntimeService().loadTransientDeployment(rsDeployment);
        logger.info("Loaded deployment {}", rsResult.deployment().id());

        final var wsResult = shared.getElementsLocal().getRuntimeService().loadTransientDeployment(wsDeployment);
        logger.info("Loaded deployment {}", wsResult.deployment().id());

    }

}
