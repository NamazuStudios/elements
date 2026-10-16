package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.PermittedTypesClassLoader;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dev.getelements.elements.sdk.ElementPathLoader.CLASSPATH_DIR;
import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_A;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static dev.getelements.elements.sdk.test.TestUtils.layoutSkeletonElement;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Regression coverage for #164: an ELM whose element carries its implementation as a <em>classpath
 * directory</em> (bare {@code .class} files, no jars) must load when staged as a zip {@link FileSystem},
 * exactly as {@code DeploymentContext#stageElmPackage} stages ELM packages in production.
 * <p>
 * The reported failure mode (3.8.16) was {@code SdkElementNotFoundException: Found no element definition
 * in ...} for a static-content-only ELM: the classpath directory lived inside the zip archive, and the
 * element definition scan could not see {@code package-info.class} through it. The static tree still
 * mounted, but only with attribute defaults since the element never loaded.
 * <p>
 * Mirrors {@link ElmDependencyOrderTest}, but unpacks the artifact's jar contents directly into
 * {@code classpath/} so the element is classpath-only.
 */
public class ElmClasspathOnlyElementTest {

    private static final TestArtifactRegistry testArtifactRegistry = new TestArtifactRegistry();

    private Path stagingDirectory;

    private FileSystem elmFileSystem;

    private MutableElementRegistry elementRegistry;

    private List<Element> loadedElements;

    @BeforeClass
    public void stageAndLoadClasspathOnlyElm() throws IOException {

        stagingDirectory = Files.createTempDirectory("elm-classpath-only-");

        // 1. Stage the element directory with the artifact's classes unpacked as bare classpath entries
        //    (no jars anywhere in the element), plus the properties files a real element carries.
        final var elementDir = stagingDirectory.resolve(VARIANT_A.getElementName());
        layoutSkeletonElement(elementDir, VARIANT_A.getAttributes());
        testArtifactRegistry.unpackArtifact(VARIANT_A, elementDir.resolve(CLASSPATH_DIR));

        // 2. Archive it as an ELM: <elementName>/classpath/** under the zip root, matching the layout
        //    a deployed ELM package has.
        final var elmFile = stagingDirectory.resolve("element.elm");
        try (var zos = new ZipOutputStream(Files.newOutputStream(elmFile));
             var walk = Files.walk(elementDir)) {
            walk.filter(Files::isRegularFile).forEach(p -> {
                try {
                    zos.putNextEntry(new ZipEntry(
                            elementDir.getParent().relativize(p).toString().replace('\\', '/')));
                    Files.copy(p, zos);
                    zos.closeEntry();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        }

        // 3. Mount the ELM as a zip FileSystem, the way the deployment staging path does.
        elmFileSystem = FileSystems.newFileSystem(elmFile);

        // 4. Load it through the standard ElementPathLoader.
        final var spiClassLoader = new URLClassLoader(
                "elm-classpath-only-test-spi",
                testArtifactRegistry.findSpiUrls(GUICE_7_0_X).toArray(URL[]::new),
                Thread.currentThread().getContextClassLoader());

        elementRegistry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loadedElements = loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(elementRegistry)
                .paths(List.of(elmFileSystem.getPath("/")))
                .parent(parent)
                .spiProvider((parentCl, path) -> spiClassLoader)
                .build()
        ).toList();

    }

    @AfterClass
    public void cleanup() throws IOException {
        if (elmFileSystem != null) {
            elmFileSystem.close();
        }
    }

    @Test
    public void testClasspathOnlyElementIsDiscovered() {
        assertEquals(loadedElements.size(), 1,
                "A classpath-only element inside an ELM zip must be discovered");
        assertEquals(loadedElements.get(0).getElementRecord().definition().name(), VARIANT_A.getElementName(),
                "The discovered element must be the archived artifact");
    }

    @Test
    public void testClasspathOnlyElementServiceAccessible() {

        final var element = loadedElements.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        final var service = element.getServiceLocator().getInstance(
                dev.getelements.elements.sdk.test.element.TestService.class);

        assertNotNull(service, "VARIANT_A must expose a TestService");
        assertEquals(service.getImplementationPackage(), VARIANT_A.getElementName());

    }

}
