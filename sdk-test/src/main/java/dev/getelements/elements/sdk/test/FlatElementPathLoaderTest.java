package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.test.element.TestService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.getelements.elements.sdk.ElementPathLoader.CLASSPATH_DIR;
import static dev.getelements.elements.sdk.test.TestElementArtifact.*;
import static dev.getelements.elements.sdk.test.TestUtils.layoutSkeletonElement;
import static java.lang.ClassLoader.getSystemClassLoader;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FlatElementPathLoaderTest {

    private static final TestArtifactRegistry testArtifactRegistry = new TestArtifactRegistry();

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(NestedElementPathLoaderTest.class);

    private final Path baseDirectory = temporaryFiles.createTempDirectory();

    private final Path variantADirectory = baseDirectory.resolve("variant_a");

    private final Path variantBDirectory = baseDirectory.resolve("variant_b");

    @BeforeClass
    public void arrangeElementsInDirectory() throws IOException {
        layoutSkeletonElement(baseDirectory, BASE.getAttributes());
        layoutSkeletonElement(variantADirectory, VARIANT_A.getAttributes());
        layoutSkeletonElement(variantBDirectory, VARIANT_B.getAttributes());
        testArtifactRegistry.unpackArtifact(VARIANT_A, variantADirectory.resolve(CLASSPATH_DIR));
        testArtifactRegistry.unpackArtifact(VARIANT_B, variantBDirectory.resolve(CLASSPATH_DIR));
    }

    @Test
    public void testLoadAll() {

        final var elementRegistry = MutableElementRegistry.newDefaultInstance();
        final var elementPathLoader = ElementPathLoader.newDefaultInstance();

        final var loadedElements = elementPathLoader.load(
                elementRegistry,
                baseDirectory,
                getSystemClassLoader()
        ).toList();

        final var inRegistry = elementRegistry.stream().toList();
        assertEquals(inRegistry.size(), 2);
        assertEquals(loadedElements.size(), 2);

        for (final var artifact : List.of(VARIANT_A, VARIANT_B)) {

            final var fromLoaded = loadedElements.stream()
                    .filter(e -> e.getElementRecord().definition().name().equals(artifact.getElementName()))
                    .findFirst()
                    .orElseThrow();

            final var fromRegistry = elementRegistry.stream()
                    .filter(e -> e.getElementRecord().definition().name().equals(artifact.getElementName()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(fromRegistry, fromLoaded);

            final var attributes = fromLoaded.getElementRecord().attributes();

            for (var entry : artifact.getAttributes().entrySet()) {
                final var attribute = attributes.getAttribute(entry.getKey().toString());
                assertEquals(attribute, entry.getValue());
            }

            final var service = fromLoaded
                    .getServiceLocator()
                    .getInstance(TestService.class);

            assertNotNull(service);

            service.testElementSpi();
            service.testElementRegistrySpi();

            assertEquals(service.getImplementationPackage(), artifact.getElementName());

        }

    }

}
