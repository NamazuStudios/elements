package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.cluster.ApplicationAssetLoader;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.test.TestArtifactRegistry;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static dev.getelements.elements.sdk.ElementPathLoader.LIB_DIR;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_WS;
import static dev.getelements.elements.sdk.test.TestElementBundle.JAKARTA_RS_DEPENDENCIES;
import static java.nio.file.Files.createDirectories;

/**
 * An implementation of the {@link ApplicationAssetLoader} which loads test {@link Element}s from the
 * {@link TestArtifactRegistry}. For the sake of testing, this will load the same test case for all applications.
 *
 */
public class TestApplicationAssetLoader implements ApplicationAssetLoader {

    private static final Logger logger = LoggerFactory.getLogger(TestApplicationAssetLoader.class);

    private static final TestArtifactRegistry registry = new TestArtifactRegistry();

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(TestApplicationAssetLoader.class);

    private static final Path testElementPath = loadTestArtifactsAndDependencies();

    private static Path loadTestArtifactsAndDependencies() {
        try {

            final var path = temporaryFiles.createTempDirectory();
            final var rsTestDirectory = path.resolve("rs").resolve(LIB_DIR);
            final var wsTestDirectory = path.resolve("ws").resolve(LIB_DIR);

            createDirectories(rsTestDirectory);
            createDirectories(wsTestDirectory);

            registry.copyArtifactTo(JAKARTA_RS, rsTestDirectory);
            registry.copyBundleTo(JAKARTA_RS_DEPENDENCIES, rsTestDirectory);

            registry.copyArtifactTo(JAKARTA_WS, wsTestDirectory);

            return path;
        } catch (Exception ex) {
            logger.error("Unable to set up test cases.", ex);
            return null;
        }
    }


    public TestApplicationAssetLoader() {
        if (testElementPath == null) {
            throw new IllegalStateException("Unable to load test element artifacts.");
        }
    }

    @Override
    public Path getAssetPath(final ApplicationId applicationId) {
        return testElementPath;
    }

}
