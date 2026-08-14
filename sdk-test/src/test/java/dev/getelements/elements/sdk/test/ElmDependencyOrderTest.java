package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.PermittedTypesClassLoader;
import dev.getelements.elements.sdk.test.element.TestService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;

import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_A;
import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_B;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Reproduces the global-deployment element discovery bug: elements packaged as ELM archives and loaded
 * together must respect {@link dev.getelements.elements.sdk.annotation.ElementDependency} ordering even
 * when the archives are supplied in reverse dependency order (B before A). The topo-sort pre-scan must
 * correctly read annotation data out of zip-nested jars and reorder elements before loading.
 *
 * <p>VARIANT_B declares {@code @ElementDependency("...element.a")}. VARIANT_A has an
 * {@code @ElementRequiredAttribute} that is intentionally absent — loading must still succeed with only a
 * warning, not an exception.</p>
 */
public class ElmDependencyOrderTest {

    private static final TestArtifactRegistry testArtifactRegistry = new TestArtifactRegistry();

    private final List<FileSystem> openFileSystems = new ArrayList<>();

    private List<Element> loadedElements;

    private MutableElementRegistry elementRegistry;

    @BeforeClass
    public void loadElementsInReverseOrder() throws IOException {

        // Open each ELM archive as a zip FileSystem so the loader sees zip-backed Paths.
        final var elmA = testArtifactRegistry.findElmPath(VARIANT_A);
        final var elmB = testArtifactRegistry.findElmPath(VARIANT_B);

        final var fsA = FileSystems.newFileSystem(elmA);
        final var fsB = FileSystems.newFileSystem(elmB);

        openFileSystems.add(fsA);
        openFileSystems.add(fsB);

        // Build the Guice SPI classloader that all elements will share for their ElementLoader SPI.
        final var spiUrls = testArtifactRegistry.findSpiUrls(GUICE_7_0_X).toArray(java.net.URL[]::new);
        final var spiClassLoader = new URLClassLoader("elm-test-spi", spiUrls, Thread.currentThread().getContextClassLoader());

        // Intentionally supply B before A to trigger the wrong-order loading scenario that the topo-sort fix addresses.
        final var paths = List.of(fsB.getPath("/"), fsA.getPath("/"));

        elementRegistry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loadedElements = loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(elementRegistry)
                .paths(paths)
                .parent(parent)
                .spiProvider((parentCl, path) -> spiClassLoader)
                .build()
        ).toList();

    }

    @AfterClass
    public void closeFileSystems() {
        for (final var fs : openFileSystems) {
            try { fs.close(); } catch (IOException ignored) {}
        }
    }

    @Test
    public void testBothElementsLoaded() {
        assertEquals(loadedElements.size(), 2, "Both VARIANT_A and VARIANT_B should be loaded");
        assertEquals(elementRegistry.stream().count(), 2, "Both elements should be in the registry");
    }

    @Test
    public void testVariantALoadedFirst() {
        // The registry stream preserves registration order; A must be registered before B.
        final var names = elementRegistry.stream()
                .map(e -> e.getElementRecord().definition().name())
                .toList();

        final int indexA = names.indexOf(VARIANT_A.getElementName());
        final int indexB = names.indexOf(VARIANT_B.getElementName());

        assertNotNull(indexA >= 0, "VARIANT_A should be in the registry");
        assertNotNull(indexB >= 0, "VARIANT_B should be in the registry");
        assertEquals(indexA < indexB, true, "VARIANT_A (dependency) must be registered before VARIANT_B (dependent)");
    }

    @Test
    public void testVariantAServiceAccessible() {
        final var elementA = elementRegistry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not found in registry"));

        final var service = elementA.getServiceLocator().getInstance(TestService.class);
        assertNotNull(service, "VARIANT_A must expose a TestService");
        assertEquals(service.getImplementationPackage(), VARIANT_A.getElementName());
    }

    @Test
    public void testVariantBServiceAccessible() {
        final var elementB = elementRegistry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_B.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_B not found in registry"));

        final var service = elementB.getServiceLocator().getInstance(TestService.class);
        assertNotNull(service, "VARIANT_B must expose a TestService");
        assertEquals(service.getImplementationPackage(), VARIANT_B.getElementName());
    }

}
