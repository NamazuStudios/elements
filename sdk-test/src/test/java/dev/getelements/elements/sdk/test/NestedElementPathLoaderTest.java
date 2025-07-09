package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.test.element.TestService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.getelements.elements.sdk.ElementPathLoader.CLASSPATH_DIR;
import static dev.getelements.elements.sdk.ElementPathLoader.LIB_DIR;
import static dev.getelements.elements.sdk.test.TestElementArtifact.*;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static dev.getelements.elements.sdk.test.TestUtils.layoutSkeletonElement;
import static java.lang.ClassLoader.getSystemClassLoader;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class NestedElementPathLoaderTest {

    private static final TestArtifactRegistry testArtifactRegistry = new TestArtifactRegistry();

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(NestedElementPathLoaderTest.class);

    private final Path baseDirectory = temporaryFiles.createTempDirectory();

    private final Path variantADirectory = baseDirectory.resolve("variant_a");

    private final Path variantBDirectory = baseDirectory.resolve("variant_b");

    private final TestElementSpi elementSpi;

    @Factory
    public static Object[] getTestFixtures() {
        return new Object[] { new NestedElementPathLoaderTest(GUICE_7_0_X)};
    }

    public NestedElementPathLoaderTest(final TestElementSpi elementSpi) {
        this.elementSpi = elementSpi;
    }

    @BeforeClass
    public void arrangeElementsInDirectory() throws IOException {
        layoutSkeletonElement(baseDirectory, BASE.getAttributes());
        layoutSkeletonElement(variantADirectory, VARIANT_A.getAttributes());
        layoutSkeletonElement(variantBDirectory, VARIANT_B.getAttributes());

        // In this configuration the base element holds the SPI, and the variants depend on that SPI in the SPI.
        testArtifactRegistry.copyArtifactTo(BASE, baseDirectory.resolve(LIB_DIR));

        // Subordinate elements depend on the parent SPI
        testArtifactRegistry.copySpiTo(elementSpi, baseDirectory.resolve(LIB_DIR));
        testArtifactRegistry.copySpiTo(elementSpi, variantADirectory.resolve(LIB_DIR));
        testArtifactRegistry.copySpiTo(elementSpi, variantBDirectory.resolve(LIB_DIR));
        testArtifactRegistry.unpackArtifact(VARIANT_A, variantADirectory.resolve(CLASSPATH_DIR));
        testArtifactRegistry.unpackArtifact(VARIANT_B, variantBDirectory.resolve(CLASSPATH_DIR));

    }

    @Test
    public void testLoadAll() throws ClassNotFoundException {

        final var elementRegistry = MutableElementRegistry.newDefaultInstance();
        final var elementPathLoader = ElementPathLoader.newDefaultInstance();

        final var loadedElements = elementPathLoader.load(
                elementRegistry,
                baseDirectory,
                getSystemClassLoader()
        ).toList();

        assertEquals(loadedElements.size(), 3);

        for (final var artifact : List.of(VARIANT_A, VARIANT_B)) {

            final var element = loadedElements.stream()
                    .filter(e -> e.getElementRecord().definition().name().equals(artifact.getElementName()))
                    .findFirst()
                    .orElseThrow();

            final var attributes = element.getElementRecord().attributes();

            for (var entry : artifact.getAttributes().entrySet()) {
                final var attribute = attributes.getAttribute(entry.getKey().toString());
                assertEquals(attribute, entry.getValue());
            }

            switch (artifact) {
                case VARIANT_A, VARIANT_B -> {
                    final var service = element
                            .getServiceLocator()
                            .getInstance(TestService.class);
                    assertNotNull(service);
                    service.testElementSpi();
                    service.testElementRegistrySpi();
                    assertEquals(service.getImplementationPackage(), artifact.getElementName());
                }
            }

        }

        final var inRegistry = elementRegistry.stream().toList();
        assertEquals(inRegistry.size(), 3);
        elementRegistry.find(BASE.getElementName()).findFirst().orElseThrow();

    }

}
