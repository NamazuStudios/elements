package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.test.element.TestService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.ElementPathLoader.*;
import static dev.getelements.elements.sdk.test.TestElementArtifact.*;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static dev.getelements.elements.sdk.test.TestUtils.layoutSkeletonElement;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.nio.file.Files.createDirectories;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ApiElementLoaderTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiElementLoaderTest.class);

    private static final TestArtifactRegistry testArtifactRegistry = new TestArtifactRegistry();

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(NestedElementPathLoaderTest.class);

    private final Path baseDirectory = temporaryFiles.createTempDirectory();

    private final Path apiDirectory = baseDirectory.resolve(API_DIR);

    private final Path variantADirectory = baseDirectory.resolve("variant_a");

    private final Path variantBDirectory = baseDirectory.resolve("variant_b");

    private final TestElementSpi elementSpi;

    @Factory
    public static Object[] getTestFixtures() {
        return new Object[] { new ApiElementLoaderTest(GUICE_7_0_X)};
    }

    public ApiElementLoaderTest(final TestElementSpi elementSpi) {
        this.elementSpi = elementSpi;
    }


    @BeforeClass
    public void arrangeElementsInDirectory() throws IOException {

        createDirectories(apiDirectory);
        testArtifactRegistry.copyArtifactTo(API, apiDirectory);

        layoutSkeletonElement(variantADirectory, VARIANT_A.getAttributes());
        layoutSkeletonElement(variantBDirectory, VARIANT_B.getAttributes());

        testArtifactRegistry.copySpiTo(elementSpi, variantADirectory.resolve(LIB_DIR));
        testArtifactRegistry.copySpiTo(elementSpi, variantBDirectory.resolve(LIB_DIR));

        testArtifactRegistry.unpackArtifact(VARIANT_A, variantADirectory.resolve(CLASSPATH_DIR));
        testArtifactRegistry.unpackArtifact(VARIANT_B, variantBDirectory.resolve(CLASSPATH_DIR));

    }

    @Test
    public void testLoad() throws ClassNotFoundException {

        final var elementRegistry = MutableElementRegistry.newDefaultInstance();
        final var elementPathLoader = ElementPathLoader.newDefaultInstance();

        final var loadedElements = elementPathLoader.load(
                elementRegistry,
                baseDirectory,
                getSystemClassLoader()
        ).toList();

        logger.info("Loaded Elements.");

        // We want to ensure that all elements root classloader is the same.
        final var loaders = loadedElements.stream()
                .map(e -> {

                    var loader = e.getElementRecord().classLoader();

                    while (loader.getParent() != null) {
                        loader = loader.getParent();
                    }

                    return loader;
                })
                .collect(toSet());

        assertEquals(loaders.size(), 1, "All elements should share the same root classloader.");

        // We want to make sure that we can locate the test interface from the root classloader.
        final var cls = loaders
                .stream()
                .findFirst()
                .get()
                .loadClass(TestService.class.getName());

        assertNotNull(cls);

    }

}
