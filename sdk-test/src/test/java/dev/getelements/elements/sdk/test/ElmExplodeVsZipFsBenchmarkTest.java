package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.PermittedTypesClassLoader;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dev.getelements.elements.sdk.ElementPathLoader.CLASSPATH_DIR;
import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_A;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static dev.getelements.elements.sdk.test.TestUtils.layoutSkeletonElement;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.testng.Assert.assertEquals;

/**
 * Throwaway benchmark for issue #69: compares cold-load time for a representative element loaded two ways:
 *
 * <ul>
 *     <li><b>zip-fs</b> — today's behavior: the {@code .elm} is mounted as an NIO zip {@link FileSystem} and
 *     classes/resources are read through it.</li>
 *     <li><b>exploded</b> — the proposed alternative: the {@code .elm} is extracted to a plain directory once
 *     up front, and loaded from there via ordinary {@code file://} URLs.</li>
 * </ul>
 *
 * <p>The element under test is a purpose-built synthetic fixture (skeleton layout + {@link TestElementArtifact
 * #VARIANT_A}'s own compiled classes only, via {@link TestArtifactRegistry#unpackArtifact}), deliberately
 * <b>without</b> the {@code lib/} jars a real packaged {@code .elm} bundles. Using a real {@code .elm}
 * (e.g. from {@link TestArtifactRegistry#findElmPath}) here fails in exploded mode: every
 * {@code sdk-test-element-*} archive bundles the base {@code sdk-test-element} module's own compiled jar
 * under {@code lib/}, which carries its own {@code @ElementDefinition}. In zip-fs mode ClassGraph can't scan
 * that nested jar at all (it's exposed via the custom {@code elm://} URL scheme), so the duplicate definition
 * is silently invisible; once exploded to a real directory ClassGraph <i>can</i> scan it and correctly rejects
 * the load as ambiguous. That's a genuine, separate finding for #69 (exploding to disk isn't just faster, it
 * makes ClassGraph see things it currently can't — including latent duplicate-definition bugs), but it means
 * a real `.elm` isn't an apples-to-apples timing fixture without first fixing that ambiguity. This fixture
 * sidesteps it so the numbers below measure loading cost alone.
 *
 * <p>This is not a hard pass/fail gate — timings are inherently noisy on a shared/dev machine and there is no
 * JMH harness in this repo to control for that (see #69). It exists purely to produce real numbers to decide
 * whether the exploded-directory approach is worth pursuing as a supported loading mode. Results are logged,
 * not asserted on, aside from a sanity check that both modes load the same element correctly.
 */
public class ElmExplodeVsZipFsBenchmarkTest {

    private static final Logger logger = LoggerFactory.getLogger(ElmExplodeVsZipFsBenchmarkTest.class);

    private static final int WARMUP_ITERATIONS = 3;

    private static final int MEASURED_ITERATIONS = 10;

    private static final TestArtifactRegistry testArtifactRegistry = new TestArtifactRegistry();

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(ElmExplodeVsZipFsBenchmarkTest.class);

    private Path elmPath;

    private Path explodedDirectory;

    @BeforeClass
    public void setUp() throws IOException {

        // Build a clean synthetic element: skeleton layout + VARIANT_A's own compiled classes only
        // (no bundled lib/ dependency jars, so there's no nested-module-definition ambiguity for
        // ClassGraph to trip on in exploded mode). The loader expects <root>/<element-dir>/{spi,lib,
        // classpath}, so the skeleton lives one level below the root we hand to the loader.
        final var sourceDirectory = temporaryFiles.createTempDirectory("source");
        final var elementDirectory = sourceDirectory.resolve("synthetic-element");
        layoutSkeletonElement(elementDirectory, VARIANT_A.getAttributes());
        testArtifactRegistry.unpackArtifact(VARIANT_A, elementDirectory.resolve(CLASSPATH_DIR));

        elmPath = temporaryFiles.createTempFile("synthetic", ".elm");
        zip(sourceDirectory, elmPath);

        explodedDirectory = temporaryFiles.createTempDirectory("exploded");

        final var explodeStart = System.nanoTime();
        explode(elmPath, explodedDirectory);
        final var explodeMillis = (System.nanoTime() - explodeStart) / 1_000_000.0;

        logger.info("Synthetic ELM under test: {} ({} bytes). One-time explode to {} took {} ms.",
                elmPath, Files.size(elmPath), explodedDirectory, explodeMillis);
    }

    @AfterClass
    public void tearDown() {
        TemporaryFiles.deleteRecursively(explodedDirectory);
    }

    /** Zips every entry of a directory tree into a single archive, preserving relative paths. */
    private static void zip(final Path sourceDirectory, final Path zipFile) throws IOException {
        try (final var zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            try (var stream = Files.walk(sourceDirectory)) {
                for (final var source : (Iterable<Path>) stream.filter(p -> !Files.isDirectory(p))::iterator) {
                    final var relative = sourceDirectory.relativize(source).toString().replace('\\', '/');
                    zos.putNextEntry(new ZipEntry(relative));
                    Files.copy(source, zos);
                    zos.closeEntry();
                }
            }
        }
    }

    /** Copies every entry of the ELM zip archive into a real directory tree, preserving relative paths. */
    private static void explode(final Path elm, final Path destination) throws IOException {
        try (final var fs = FileSystems.newFileSystem(elm)) {
            final var root = fs.getPath("/");
            try (var stream = Files.walk(root)) {
                for (final var source : (Iterable<Path>) stream::iterator) {
                    final var relative = root.relativize(source).toString();
                    if (relative.isEmpty()) {
                        continue;
                    }
                    final var target = destination.resolve(relative);
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private URLClassLoader newSpiClassLoader() {
        final var spiUrls = testArtifactRegistry.findSpiUrls(GUICE_7_0_X).toArray(URL[]::new);
        return new URLClassLoader("elm-benchmark-spi", spiUrls, Thread.currentThread().getContextClassLoader());
    }

    private List<Element> loadFromZipFs() throws IOException {
        try (final var fs = FileSystems.newFileSystem(elmPath);
             final var spiCl = newSpiClassLoader()) {

            final var elementRegistry = MutableElementRegistry.newDefaultInstance();
            final var loader = ElementPathLoader.newDefaultInstance();
            final var parent = new PermittedTypesClassLoader();

            try {
                return loader.load(ElementPathLoader.LoadConfiguration.builder()
                        .registry(elementRegistry)
                        .paths(List.of(fs.getPath("/")))
                        .parent(parent)
                        .spiProvider((parentCl, path) -> spiCl)
                        .build()
                ).toList();
            } finally {
                elementRegistry.close();
            }
        }
    }

    private List<Element> loadFromExplodedDirectory() throws IOException {

        try (final var spiCl = newSpiClassLoader()) {

            final var elementRegistry = MutableElementRegistry.newDefaultInstance();
            final var loader = ElementPathLoader.newDefaultInstance();
            final var parent = new PermittedTypesClassLoader();

            try {
                return loader.load(ElementPathLoader.LoadConfiguration.builder()
                        .registry(elementRegistry)
                        .paths(List.of(explodedDirectory))
                        .parent(parent)
                        .spiProvider((parentCl, path) -> spiCl)
                        .build()
                ).toList();
            } finally {
                elementRegistry.close();
            }
        }
    }

    @Test
    public void benchmarkZipFsVsExplodedDirectoryLoad() throws IOException {

        // Sanity check both paths actually load the element correctly before trusting any timing numbers.
        final var zipFsLoaded = loadFromZipFs();
        assertEquals(zipFsLoaded.size(), 1, "zip-fs mode should load exactly one element");

        final var explodedLoaded = loadFromExplodedDirectory();
        assertEquals(explodedLoaded.size(), 1, "exploded-directory mode should load exactly one element");

        assertEquals(
                explodedLoaded.get(0).getElementRecord().definition().name(),
                zipFsLoaded.get(0).getElementRecord().definition().name(),
                "both modes should load the same element"
        );

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            loadFromZipFs();
            loadFromExplodedDirectory();
        }

        final var zipFsTimingsMs = new double[MEASURED_ITERATIONS];
        final var explodedTimingsMs = new double[MEASURED_ITERATIONS];

        for (int i = 0; i < MEASURED_ITERATIONS; i++) {

            final var zipFsStart = System.nanoTime();
            loadFromZipFs();
            zipFsTimingsMs[i] = (System.nanoTime() - zipFsStart) / 1_000_000.0;

            final var explodedStart = System.nanoTime();
            loadFromExplodedDirectory();
            explodedTimingsMs[i] = (System.nanoTime() - explodedStart) / 1_000_000.0;
        }

        logger.info("zip-fs load times (ms):     {}", Arrays.toString(zipFsTimingsMs));
        logger.info("exploded load times (ms):   {}", Arrays.toString(explodedTimingsMs));
        logger.info("zip-fs load avg (ms):       {}", average(zipFsTimingsMs));
        logger.info("exploded load avg (ms):     {}", average(explodedTimingsMs));
    }

    private static double average(final double[] values) {
        var sum = 0.0;
        for (final var value : values) {
            sum += value;
        }
        return sum / values.length;
    }

}
