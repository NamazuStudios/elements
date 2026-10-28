package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementPathRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementStaticContentRecord;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import dev.getelements.elements.servlet.HttpContextRoot;
import org.eclipse.jetty.server.Handler;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Covers the on-disk static content override (#146): when the element's attributes name a directory
 * ({@code dev.getelements.element.static.directory} for the standard loader,
 * {@code dev.getelements.element.ui.directory} for the UI loader), the packaged content is overridden
 * entirely and content is served from disk -- for rapid iteration without repackaging the element.
 * <p>
 * The permutation matrix: no attribute (packaged content used), attribute present with a populated
 * directory (disk wins, packaged resolver not invoked), missing directory (warning + packaged fallback),
 * blank attribute (ignored), each loader reading only its own key, nested directory trees indexed, and
 * the empty-directory case (skipped as if no content existed).
 */
public class StaticContentLoaderDiskOverrideTest {

    private static final String ELEMENT_NAME = "test.element";

    private final TemporaryFiles temporaryFiles = new TemporaryFiles(StaticContentLoaderDiskOverrideTest.class);

    private Path elementDirectory;

    private HttpPathRegistry httpPathRegistry;

    private Handler.Sequence sequence;

    private List<String> logs;

    private List<String> warnings;

    private List<Element> servedElements;

    private List<URI> uris;

    private boolean packagedResolverInvoked;

    @BeforeMethod
    public void setup() throws IOException {

        elementDirectory = temporaryFiles.createTempDirectory("element-under-test-");
        Files.createDirectories(elementDirectory.resolve(ElementPathLoader.STATIC_DIR));
        Files.createDirectories(elementDirectory.resolve(ElementPathLoader.UI_DIR));

        httpPathRegistry = new HttpPathRegistry();
        sequence = new Handler.Sequence();

        logs = new CopyOnWriteArrayList<>();
        warnings = new CopyOnWriteArrayList<>();
        servedElements = new CopyOnWriteArrayList<>();
        uris = new CopyOnWriteArrayList<>();

        packagedResolverInvoked = false;

    }

    @AfterMethod
    public void cleanup() {
        try {
            sequence.stop();
        } catch (Exception ignored) {}
    }

    // -- Good input: the override serves from disk, bypassing the packaged tree ----------------------

    @Test
    public void standardLoaderServesContentFromDiskWhenAttributePresent() throws IOException {

        final var disk = stageDiskTree("static-from-disk-", "index.html", "nested/app.js");

        final var loader = standardLoader();
        final var element = elementWith(attributesWith(
                ElementContainerService.STATIC_CONTENT_DIRECTORY, disk.toString()));

        loader.load(pendingDeployment(), runtimeRecord(element), element);

        assertFalse(packagedResolverInvoked, "the packaged content resolver must not be consulted");
        assertTrue(servedElements.contains(element), "the element must be served");

        // Every entry of the disk tree is indexed; whether directory entries themselves are excluded
        // from the serving index is the StaticRuleEngine's concern (covered by StaticRuleEngineTest).
        assertEquals(servingCount(logs), walkedEntryCount(disk), "the disk tree must be indexed, was: " + logs);
        assertTrue(logs.stream().anyMatch(l -> l.contains("from disk")), "the disk override must be logged");

    }

    @Test
    public void uiLoaderServesContentFromDiskWhenAttributePresent() throws IOException {

        final var disk = stageDiskTree("ui-from-disk-", "index.html");

        final var loader = uiLoader();
        final var element = elementWith(attributesWith(
                ElementContainerService.UI_CONTENT_DIRECTORY, disk.toString()));

        loader.load(pendingDeployment(), runtimeRecord(element), element);

        assertFalse(packagedResolverInvoked, "the packaged content resolver must not be consulted");
        assertTrue(servedElements.contains(element));

    }

    @Test
    public void relativeDirectoryResolvesAgainstWorkingDirectory() throws IOException {

        // A relative path resolves against the server's working directory.
        final var relative = "static-disk-override-test-" + System.nanoTime();
        final var disk = Path.of(relative).toAbsolutePath();
        try {
            Files.createDirectories(disk);
            Files.writeString(disk.resolve("index.html"), "hello");

            final var loader = standardLoader();
            final var element = elementWith(attributesWith(
                    ElementContainerService.STATIC_CONTENT_DIRECTORY, relative));

            loader.load(pendingDeployment(), runtimeRecord(element), element);

            assertFalse(packagedResolverInvoked);
            assertTrue(servedElements.contains(element));
            assertTrue(logs.stream().anyMatch(l -> l.contains(relative)),
                    "the configured path must be logged, was: " + logs);
        } finally {
            TemporaryFiles.deleteRecursively(disk);
        }

    }

    // -- Fallbacks: bad/missing configuration falls back to the packaged content ---------------------

    @Test
    public void missingAttributeUsesPackagedContent() throws IOException {

        stagePackagedFile("index.html");
        final var loader = standardLoader();
        final var element = elementWith(Attributes.emptyAttributes());

        loader.load(pendingDeployment(), runtimeRecord(element), element);

        assertTrue(packagedResolverInvoked, "the packaged content resolver must be consulted");
        assertTrue(servedElements.contains(element));

    }

    @Test
    public void blankAttributeIsIgnored() throws IOException {

        stagePackagedFile("index.html");
        final var loader = standardLoader();
        final var element = elementWith(attributesWith(
                ElementContainerService.STATIC_CONTENT_DIRECTORY, "   "));

        loader.load(pendingDeployment(), runtimeRecord(element), element);

        assertTrue(packagedResolverInvoked);
        assertTrue(warnings.isEmpty(), "a blank attribute is simply absent, not a warning: " + warnings);

    }

    @Test
    public void missingDirectoryFallsBackToPackagedContentWithWarning() throws IOException {

        stagePackagedFile("index.html");
        final var nonexistent = temporaryFiles.createTempDirectory("missing-").resolve("does-not-exist");

        final var loader = standardLoader();
        final var element = elementWith(attributesWith(
                ElementContainerService.STATIC_CONTENT_DIRECTORY, nonexistent.toString()));

        loader.load(pendingDeployment(), runtimeRecord(element), element);

        assertTrue(packagedResolverInvoked, "a missing directory must fall back to the packaged content");
        assertTrue(servedElements.contains(element));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("does not exist")),
                "the fallback must be logged as a warning, was: " + warnings);

    }

    @Test
    public void emptyDirectorySkipsStaticContent() throws IOException {

        final var disk = temporaryFiles.createTempDirectory("empty-disk-");

        final var loader = standardLoader();
        final var element = elementWith(attributesWith(
                ElementContainerService.STATIC_CONTENT_DIRECTORY, disk.toString()));

        loader.load(pendingDeployment(), runtimeRecord(element), element);

        assertFalse(packagedResolverInvoked, "the override applies even when the directory is empty");
        assertTrue(servedElements.isEmpty(), "an empty content tree must not mount a handler");
        assertTrue(logs.stream().anyMatch(l -> l.contains("No static files found")));

    }

    // -- Namespacing: each loader reads only its own key ---------------------------------------------

    @Test
    public void standardLoaderIgnoresTheUiDirectoryAttribute() throws IOException {

        stagePackagedFile("index.html");
        final var disk = stageDiskTree("ui-only-", "index.html");

        final var loader = standardLoader();
        final var element = elementWith(attributesWith(
                ElementContainerService.UI_CONTENT_DIRECTORY, disk.toString()));

        loader.load(pendingDeployment(), runtimeRecord(element), element);

        assertTrue(packagedResolverInvoked, "the standard loader must not honor the ui.directory key");
        assertTrue(servedElements.contains(element));

    }

    @Test
    public void uiLoaderIgnoresTheStaticDirectoryAttribute() throws IOException {

        stagePackagedUiFile("index.html");
        final var disk = stageDiskTree("static-only-", "robots.txt");

        final var loader = uiLoader();
        final var element = elementWith(attributesWith(
                ElementContainerService.STATIC_CONTENT_DIRECTORY, disk.toString()));

        loader.load(pendingDeployment(), runtimeRecord(element), element);

        assertTrue(packagedResolverInvoked, "the ui loader must not honor the static.directory key");
        assertTrue(servedElements.contains(element));

    }

    // -- Fixtures ------------------------------------------------------------------------------------

    private Path stageDiskTree(final String prefix, final String... files) throws IOException {
        final var disk = temporaryFiles.createTempDirectory(prefix);
        for (final var file : files) {
            final var path = disk.resolve(file);
            Files.createDirectories(path.getParent());
            Files.writeString(path, "content of " + file);
        }
        return disk;
    }

    private void stagePackagedFile(final String file) throws IOException {
        Files.writeString(elementDirectory.resolve(ElementPathLoader.STATIC_DIR).resolve(file), "packaged");
    }

    private void stagePackagedUiFile(final String file) throws IOException {
        Files.writeString(elementDirectory.resolve(ElementPathLoader.UI_DIR).resolve(file), "packaged");
    }

    private Element elementWith(final Attributes attributes) {

        final var definition = mock(ElementDefinitionRecord.class);
        when(definition.name()).thenReturn(ELEMENT_NAME);

        final var elementRecord = mock(ElementRecord.class);
        when(elementRecord.definition()).thenReturn(definition);
        when(elementRecord.attributes()).thenReturn(attributes);

        final var element = mock(Element.class);
        when(element.getElementRecord()).thenReturn(elementRecord);
        return element;

    }

    private ElementRuntimeService.RuntimeRecord runtimeRecord(final Element element) {
        return new ElementRuntimeService.RuntimeRecord(
                deployment(),
                ElementRuntimeService.RuntimeStatus.CLEAN,
                false,
                null,
                List.of(element),
                List.of(),
                List.of(),
                List.of(elementDirectory),
                List.of(),
                null,
                Map.of(element, mock(ElementPathRecord.class)),
                null,
                null,
                null
        );
    }

    private dev.getelements.elements.sdk.model.system.ElementDeployment deployment() {
        final var deployment = mock(dev.getelements.elements.sdk.model.system.ElementDeployment.class);
        when(deployment.id()).thenReturn("deployment-1");
        return deployment;
    }

    private Loader.PendingDeployment pendingDeployment() {
        return new Loader.PendingDeployment(
                uris::add,
                logs::add,
                warnings::add,
                error -> {},
                servedElements::add
        );
    }

    private StaticContentLoader loader(final String defaultFormat,
                                       final String uriKey,
                                       final String directoryKey,
                                       final String packagedDir) {

        final Function<ElementPathRecord, ElementStaticContentRecord> resolve = record -> {
            packagedResolverInvoked = true;
            return packagedTree(packagedDir);
        };

        final var httpContextRoot = new HttpContextRoot();
        httpContextRoot.setHttpPathPrefix("");

        final var loader = new StaticContentLoader(resolve, defaultFormat, uriKey, directoryKey, "static") {};
        loader.setSequence(sequence);
        loader.setHttpContextRoot(httpContextRoot);
        loader.setAppOutsideUrl("https://server.example.com");
        loader.setPublicMaxAgeSeconds(60);
        loader.setHttpPathRegistry(httpPathRegistry);
        return loader;

    }

    private StaticContentLoader standardLoader() {
        return loader("/app/static/%s",
                ElementContainerService.STATIC_CONTENT_URI,
                ElementContainerService.STATIC_CONTENT_DIRECTORY,
                ElementPathLoader.STATIC_DIR);
    }

    private StaticContentLoader uiLoader() {
        return loader("/app/ui/%s",
                ElementContainerService.UI_CONTENT_URI,
                ElementContainerService.UI_CONTENT_DIRECTORY,
                ElementPathLoader.UI_DIR);
    }

    private ElementStaticContentRecord packagedTree(final String dir) {
        final var packagedRoot = elementDirectory.resolve(dir);
        try (var walk = Files.walk(packagedRoot)) {
            final var contents = walk.filter(p -> !p.equals(packagedRoot)).toList();
            return new ElementStaticContentRecord(packagedRoot, contents);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static int walkedEntryCount(final Path dir) throws IOException {
        // Directory entries are excluded from the serving index by the StaticRuleEngine (covered by
        // StaticRuleEngineTest); only the regular files the loader will actually serve are counted.
        try (var walk = Files.walk(dir)) {
            return (int) walk.filter(p -> !p.equals(dir)).filter(Files::isRegularFile).count();
        }
    }

    private static int servingCount(final List<String> logs) {
        return logs.stream()
                .filter(l -> l.contains("Serving") && l.contains("static file(s)"))
                .mapToInt(l -> Integer.parseInt(l.replaceAll(".*Serving (\\d+) static file.*", "$1")))
                .findFirst()
                .orElse(-1);
    }

    private static Attributes attributesWith(final String key, final String value) {
        return new SimpleAttributes.Builder().setAttribute(key, value).build();
    }

}
