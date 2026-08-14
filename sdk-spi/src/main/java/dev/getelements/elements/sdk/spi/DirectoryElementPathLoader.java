package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDependencies;
import dev.getelements.elements.sdk.annotation.ElementDependency;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementDependencyRecord;
import dev.getelements.elements.sdk.record.ElementManifestRecord;
import dev.getelements.elements.sdk.record.ElementPathRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementStaticContentRecord;
import dev.getelements.elements.sdk.util.PropertiesAttributes;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.nio.file.Files.*;

public class DirectoryElementPathLoader implements ElementPathLoader {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryElementPathLoader.class);

    private static boolean isPathInHiddenHierarchy(Path path) {

        boolean isHidden;

        try {

            path = path.toAbsolutePath();

            do {
                isHidden = isHidden(path);
                path = path.getParent();
            } while (path != null && !isHidden);

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return isHidden;

    }

    private static URL toUrl(final Path path) {
        try {

            final var url = path.toUri().toURL();
            final var urlString = url.toString();

            if (isRegularFile(path) || isDirectory(path) && urlString.endsWith("/")) {
                return url;
            }

            final var directoryUrlString = "%s/".formatted(urlString);
            return new URL(directoryUrlString);

        } catch (MalformedURLException ex) {
            throw new SdkException(ex);
        }
    }

    private static boolean isJarFile(final Path path) {
        return isRegularFile(path) && path.getFileName().toString().endsWith("."  + JAR_EXTENSION);
    }

    private static boolean isApiDirectory(final Path path) {
        return isDirectory(path) && API_DIR.equals(path.getFileName().toString());
    }

    private static boolean isSpiDirectory(final Path path) {
        return isDirectory(path) && SPI_DIR.equals(path.getFileName().toString());
    }

    private static boolean isLibDirectory(final Path path) {
        return isDirectory(path) && LIB_DIR.equals(path.getFileName().toString());
    }

    private static boolean isClasspathDirectory(final Path path) {
        return isDirectory(path) && CLASSPATH_DIR.equals(path.getFileName().toString());
    }

    private static boolean isAttributesFiles(final Path path) {
        return isRegularFile(path) && ATTRIBUTES_PROPERTIES_FILE.equals(path.getFileName().toString());
    }

    @Override
    public Attributes readManifest(final Path path) {

        final var manifestPath = path.resolve(MANIFEST_PROPERTIES_FILE);

        if (!isRegularFile(manifestPath)) {
            return Attributes.emptyAttributes();
        }

        final var properties = new Properties();

        try (final var is = newInputStream(manifestPath)) {
            properties.load(is);
        } catch (IOException ex) {
            logger.warn("Failed to read manifest at {}: {}", manifestPath, ex.getMessage());
            return Attributes.emptyAttributes();
        }

        return PropertiesAttributes.wrap(properties);

    }

    private Attributes readAttributes(final Path path) {

        final var attributesPath = path.resolve(ATTRIBUTES_PROPERTIES_FILE);

        if (!isRegularFile(attributesPath)) {
            return Attributes.emptyAttributes();
        }

        final var properties = new Properties();

        try (final var is = newInputStream(attributesPath)) {
            properties.load(is);
        } catch (IOException ex) {
            logger.warn("Failed to read attributes at {}: {}", attributesPath, ex.getMessage());
            return Attributes.emptyAttributes();
        }

        return PropertiesAttributes.wrap(properties);

    }

    @Override
    public ElementPathRecord readElement(final Path path) {

        final var attributes = readAttributes(path);
        final var manifest = ElementManifestRecord.from(readManifest(path));

        final var apiRoot = path.resolve(API_DIR);
        final var spiRoot = path.resolve(SPI_DIR);
        final var libRoot = path.resolve(LIB_DIR);
        final var classpathRoot = path.resolve(CLASSPATH_DIR);
        final var uiContentRoot = path.resolve(path.resolve(UI_DIR));
        final var staticContentRoot = path.resolve(path.resolve(STATIC_DIR));

        final var api = collectDirEntries(apiRoot);
        final var spi = collectDirEntries(spiRoot);
        final var lib = collectDirEntries(libRoot);
        final var classpath = collectDirEntriesRecursive(classpathRoot);
        final var uiContent = collectDirEntriesRecursive(uiContentRoot);
        final var staticContent = collectDirEntriesRecursive(staticContentRoot);

        return new ElementPathRecord(
                path,
                api,
                spi,
                lib,
                classpath,
                uiContent,
                staticContent,
                attributes,
                manifest,
                isDirectory(uiContentRoot)
                        ? new ElementStaticContentRecord(uiContentRoot, uiContent)
                        : null,
                isDirectory(staticContentRoot)
                        ? new ElementStaticContentRecord(staticContentRoot, staticContent)
                        : null
        );

    }

    private List<Path> collectDirEntries(final Path dir) {

        if (!isDirectory(dir)) {
            return List.of();
        }

        final var entries = new ArrayList<Path>();

        try (final var ds = newDirectoryStream(dir)) {
            for (final var entry : ds) {
                entries.add(entry);
            }
        } catch (IOException ex) {
            throw new SdkException(ex);
        }

        return List.copyOf(entries);

    }

    private List<Path> collectDirEntriesRecursive(final Path dir) {

        if (!isDirectory(dir)) {
            return List.of();
        }

        try (final var walk = Files.walk(dir)) {
            return walk
                    .filter(p -> !p.equals(dir))
                    .toList();
        } catch (IOException ex) {
            throw new SdkException(ex);
        }

    }

    @Override
    public Stream<ElementPathRecord> readElementPaths(final Path path) {

        if (!isDirectory(path)) {
            return Stream.empty();
        }

        final var records = new ArrayList<ElementPathRecord>();

        try (final var ds = newDirectoryStream(path)) {
            for (final var subpath : ds) {
                if (isDirectory(subpath) && !isApiDirectory(subpath) && !isPathInHiddenHierarchy(subpath)) {
                    records.add(readElement(subpath));
                }
            }
        } catch (IOException ex) {
            throw new SdkException(ex);
        }

        return records.stream();

    }

    @Override
    public URLClassLoader buildApiClassLoader(final ClassLoader parent, final Collection<Path> paths) {
        return buildJarClassLoader(parent, paths, this::collectApiJars, "API");
    }

    @Override
    public Stream<Element> load(final LoadConfiguration config) {

        final var elements = new ArrayList<Element>();
        final var apiClassLoader = buildApiClassLoader(config.parent(), config.paths());

        try {

            // Collect all element subdirectories across all staged paths, then pre-scan each for
            // @ElementDefinition name and @ElementDependency declarations so we can topologically sort
            // them before loading. This ensures dependencies are always registered in the ElementRegistry
            // before the elements that depend on them are wired by Guice.
            final var allSubdirs = collectElementSubdirs(config.paths());

            final var scanRecords = allSubdirs.stream()
                    .map(subdir -> preScanSubdir(subdir, apiClassLoader, config))
                    .toList();

            final var sortedSubdirs = topoSortSubdirs(scanRecords);

            // Load elements in dependency order
            for (final var subdir : sortedSubdirs) {

                ClassLoader spiClassLoader = apiClassLoader;
                ClassLoader elementClassLoader = null;

                try (final var elementDirectory = newDirectoryStream(subdir)) {

                    spiClassLoader = findSpiClassLoader(apiClassLoader, subdir).orElse(apiClassLoader);
                    elementClassLoader = config.spiLoader().apply(spiClassLoader, subdir);

                    final var record = ElementPathLoaderRecord.from(
                            config.baseAttributes(),
                            config.registry(),
                            elementClassLoader,
                            config.baseClassLoader(),
                            subdir,
                            elementDirectory,
                            config.attributesProvider()
                    );

                    if (record.isValidElement()) {
                        try {
                            final var element = record.loadElement(config.failedElementHandler());
                            elements.add(element);
                            config.elementLoadedHandler().accept(subdir, element);

                            // spiClassLoader/elementClassLoader sit between the (ref-counted, shared)
                            // apiClassLoader and the element's own implementation classloader.
                            // URLClassLoader.close() never cascades to its parent, so unlike the impl
                            // classloader (closed via GuiceSdkElement#close), these are otherwise only
                            // released by GC — leaking SPI jar file handles on every redeploy. Close them
                            // once the element itself is closed, but never close apiClassLoader itself:
                            // when no SPI is configured both variables fall back to apiClassLoader, and
                            // it's shared/ref-counted across every element loaded from this deployment.
                            final var spiClassLoaderToClose = spiClassLoader == apiClassLoader ? null : spiClassLoader;
                            final var elementClassLoaderToClose =
                                    elementClassLoader == apiClassLoader || elementClassLoader == spiClassLoaderToClose
                                            ? null
                                            : elementClassLoader;

                            if (spiClassLoaderToClose != null || elementClassLoaderToClose != null) {
                                element.onClose(el -> {
                                    if (spiClassLoaderToClose != null) closeClassLoader(spiClassLoaderToClose);
                                    if (elementClassLoaderToClose != null) closeClassLoader(elementClassLoaderToClose);
                                });
                            }
                        } catch (final Throwable t) {

                            if (t instanceof SdkException sdkEx) {
                                logger.warn("Caught exception loading element. Deferring to handler.", sdkEx);
                                config.sdkExceptionHandler().accept(sdkEx);
                            } else {
                                config.sdkExceptionHandler().accept(new SdkException(t));
                                logger.error("Caught exception loading element. Skipping.", t);
                            }

                            // Close classloaders on failure to release OS file handles. On Windows,
                            // URLClassLoaders hold exclusive locks on their JAR files until closed,
                            // preventing temp directory cleanup after a failed element load.

                            // Only close spiClassLoader if it's distinct from apiClassLoader —
                            // when findSpiClassLoader returns empty, spiClassLoader IS apiClassLoader
                            // and closing it would break all subsequent element loads.

                            if (spiClassLoader != apiClassLoader) closeClassLoader(spiClassLoader);
                            closeClassLoader(elementClassLoader);

                        }
                    }

                } catch (IOException ex) {
                    logger.warn("Failed to open element directory {}: {}", subdir, ex.getMessage());
                    config.sdkExceptionHandler().accept(new SdkException(ex));
                    if (spiClassLoader != apiClassLoader) closeClassLoader(spiClassLoader);
                    if (elementClassLoader != null) closeClassLoader(elementClassLoader);
                }

            }

            // Attach close handlers to all elements for reference counting. This ensures that any FileSystems
            // referenced by the underlying API Classpath are closed when all Elements no longer need them.
            // These are likely open file descriptors to the backing ELM files.

            if (!elements.isEmpty()) {

                final var counter = new AtomicInteger(elements.size());

                elements.forEach(element -> element.onClose(el -> {
                    if (counter.decrementAndGet() == 0) {
                        try {
                            apiClassLoader.close();
                        } catch (IOException ex) {
                            logger.error("Caught exception closing API Classloader.", ex);
                            throw new SdkException("Error closing API classloader.", ex);
                        }
                    }
                }));

            } else {
                // No elements loaded, close the API classloader immediately
                try {
                    apiClassLoader.close();
                } catch (IOException ex) {
                    throw new SdkException(ex);
                }
            }

        } catch (Exception ex) {

            logger.error("Caught exception loading Element.", ex);

            for (var element : elements) {
                try {
                    element.close();
                } catch (Exception e) {
                    logger.error("Caught exception closing previously loaded Element.", e);
                    ex.addSuppressed(e);
                }
            }

            try {
                apiClassLoader.close();
            } catch (IOException e) {
                logger.error("Caught exception closing api classloader.", e);
                ex.addSuppressed(e);
            }

            throw ex;

        }

        return elements.stream();

    }

    /**
     * Collects all element subdirectories from the given top-level paths. Each top-level path
     * is either a real filesystem directory or the root of an already-open zip FileSystem (ELM).
     * Non-API, non-hidden immediate subdirectories are treated as element subdirs.
     */
    private List<Path> collectElementSubdirs(final Collection<Path> topLevelPaths) {

        final var result = new ArrayList<Path>();

        for (final var path : topLevelPaths) {

            if (!Files.isDirectory(path)) {
                logger.debug("Skipping non-directory path {} during element subdir collection", path);
                continue;
            }

            try (final var ds = newDirectoryStream(path)) {
                for (final var subpath : ds) {
                    if (isDirectory(subpath) && !isApiDirectory(subpath) && !isPathInHiddenHierarchy(subpath)) {
                        result.add(subpath);
                    }
                }
            } catch (IOException ex) {
                logger.warn("Failed to list element subdirs from {}: {}", path, ex.getMessage());
            }

        }

        return result;

    }

    /**
     * Pre-scans a single element subdirectory using ClassGraph to locate the element's declared
     * name ({@link ElementDefinition}) and dependency names ({@link ElementDependency}) without
     * loading any classes.
     *
     * <p>ClassGraph cannot scan jars via custom {@code elm://} URLs (which {@link UrlUtils#forPath}
     * produces for jars nested inside ELM zip archives). To work around this, jars that live inside
     * a zip {@link FileSystem} are extracted to temporary {@code file://} paths before the scan and
     * deleted immediately afterward. This ensures the topo-sort has correct dependency information
     * regardless of whether elements reside on disk or inside ELM archives.</p>
     *
     * <p>Failures are treated as "no declared name / no dependencies" so that ordering degrades
     * gracefully rather than preventing the deployment from loading at all.</p>
     */
    private ElementScanRecord preScanSubdir(
            final Path subdir,
            final URLClassLoader apiClassLoader,
            final LoadConfiguration config) {

        final var tempJars = new ArrayList<Path>();

        try {

            final var libDir = subdir.resolve(LIB_DIR);
            final var classpathDir = subdir.resolve(CLASSPATH_DIR);

            final var urls = new ArrayList<URL>();

            if (isDirectory(libDir)) {
                try (final var ds = newDirectoryStream(libDir)) {
                    for (final var jar : ds) {
                        if (isJarFile(jar)) {
                            if (jar.getFileSystem() == FileSystems.getDefault()) {
                                urls.add(jar.toUri().toURL());
                            } else {
                                // Jar is inside a zip FileSystem (e.g. an ELM archive). ClassGraph
                                // cannot scan elm:// URLs, so extract to a temp file with a file:// URL.
                                final var tempJar = Files.createTempFile("prescan-", ".jar");
                                Files.copy(jar, tempJar, StandardCopyOption.REPLACE_EXISTING);
                                tempJars.add(tempJar);
                                urls.add(tempJar.toUri().toURL());
                            }
                        }
                    }
                }
            }

            if (isDirectory(classpathDir)) {
                if (classpathDir.getFileSystem() == FileSystems.getDefault()) {
                    urls.add(classpathDir.toUri().toURL());
                } else {
                    // Classpath dir is inside a zip FileSystem. Extract its contents to a temp directory
                    // with file:// URLs so ClassGraph can scan them.
                    final var tempClasspathDir = Files.createTempDirectory("prescan-cp-");
                    tempJars.add(tempClasspathDir);
                    try (final var walk = Files.walk(classpathDir)) {
                        walk.filter(p -> !p.equals(classpathDir)).forEach(src -> {
                            try {
                                final var relative = classpathDir.relativize(src);
                                final var dest = tempClasspathDir.resolve(relative.toString());
                                if (isDirectory(src)) {
                                    Files.createDirectories(dest);
                                } else {
                                    Files.createDirectories(dest.getParent());
                                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException ex) {
                                throw new java.io.UncheckedIOException(ex);
                            }
                        });
                    }
                    final var tempUrl = tempClasspathDir.toUri().toURL();
                    final var tempUrlStr = tempUrl.toString();
                    urls.add(tempUrlStr.endsWith("/") ? tempUrl : new URL(tempUrlStr + "/"));
                }
            }

            if (urls.isEmpty()) {
                return new ElementScanRecord(subdir, null, List.of());
            }

            try (final var lightCL = new URLClassLoader(
                    "prescan[%s]".formatted(subdir),
                    urls.toArray(URL[]::new),
                    null)) {

                final var cg = new ClassGraph()
                        .overrideClassLoaders(lightCL)
                        .ignoreParentClassLoaders()
                        .enableClassInfo()
                        .enableAnnotationInfo();

                try (final var result = cg.scan()) {

                    final var pkgInfoOpt = result.getPackageInfo().stream()
                            .filter(nfo -> nfo.hasAnnotation(ElementDefinition.class.getName()))
                            .findFirst();

                    if (pkgInfoOpt.isEmpty()) {
                        return new ElementScanRecord(subdir, null, List.of());
                    }

                    final var pkgInfo = pkgInfoOpt.get();

                    // Read element name from @ElementDefinition(value="..."); falls back to package name.
                    final var defAnnot = pkgInfo.getAnnotationInfo(ElementDefinition.class.getName());
                    final String rawName = defAnnot != null
                            ? (String) defAnnot.getParameterValues().getValue("value")
                            : null;
                    final String elementName = (rawName == null || rawName.isBlank())
                            ? pkgInfo.getName()
                            : rawName;

                    // Read dependency names from @ElementDependency. Java stores a single annotation
                    // directly; multiple are wrapped by the compiler in @ElementDependencies.
                    final var depNames = new ArrayList<String>();
                    collectDependencyNames(pkgInfo.getAnnotationInfo(ElementDependency.class.getName()), depNames);
                    collectDependencyNamesFromContainer(
                            pkgInfo.getAnnotationInfo(ElementDependencies.class.getName()), depNames);

                    logger.debug("Pre-scanned element '{}' at {}: dependencies={}", elementName, subdir, depNames);
                    return new ElementScanRecord(subdir, elementName, List.copyOf(depNames));

                }

            }

        } catch (Exception ex) {
            logger.warn("Pre-scan failed for element at {} ({}). Loading in original order.", subdir, ex.getMessage());
            return new ElementScanRecord(subdir, null, List.of());
        } finally {
            for (final var tempPath : tempJars) {
                try {
                    deleteRecursive(tempPath);
                } catch (IOException ex) {
                    logger.warn("Failed to delete pre-scan temp path {}", tempPath, ex);
                }
            }
        }

    }

    private static void deleteRecursive(final Path path) throws IOException {
        if (!Files.exists(path)) return;
        if (isDirectory(path)) {
            try (final var walk = Files.walk(path)) {
                walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); }
                    catch (IOException ex) { throw new UncheckedIOException(ex); }
                });
            }
        } else {
            Files.deleteIfExists(path);
        }
    }

    private static void collectDependencyNames(final AnnotationInfo depAnnot, final List<String> out) {
        if (depAnnot == null) return;
        final var v = depAnnot.getParameterValues().getValue("value");
        if (v instanceof String s && !s.isBlank()) out.add(s);
    }

    private static void collectDependencyNamesFromContainer(
            final AnnotationInfo containerAnnot, final List<String> out) {
        if (containerAnnot == null) return;
        final var arr = containerAnnot.getParameterValues().getValue("value");
        if (arr instanceof Object[] elements) {
            for (final var elem : elements) {
                if (elem instanceof AnnotationInfo depAnnot) {
                    collectDependencyNames(depAnnot, out);
                }
            }
        }
    }

    /**
     * Topologically sorts element subdirectories using Kahn's BFS algorithm so that each element
     * is loaded after all elements it depends on (via {@code @ElementDependency}).
     *
     * <p>Dependencies that do not appear in the scan results (i.e. satisfied by elements from a
     * different deployment) are silently ignored for ordering purposes. Cycles are detected,
     * logged as an error, and broken by appending the cyclic elements in their original order.</p>
     */
    private List<Path> topoSortSubdirs(final List<ElementScanRecord> scanRecords) {

        // Map element name -> its subdir, for dependency lookup
        final var nameToSubdir = new HashMap<String, Path>();
        for (final var rec : scanRecords) {
            if (rec.elementName() != null) {
                nameToSubdir.put(rec.elementName(), rec.subdir());
            }
        }

        // Build in-degree map (LinkedHashMap preserves insertion / original order as tie-breaker)
        // and reverse adjacency: dependency subdir -> list of subdirs that depend on it
        final var inDegree = new LinkedHashMap<Path, Integer>();
        final var dependents = new HashMap<Path, List<Path>>();

        for (final var rec : scanRecords) {
            inDegree.put(rec.subdir(), 0);
        }

        for (final var rec : scanRecords) {
            for (final var depName : rec.dependencyNames()) {
                final var depSubdir = nameToSubdir.get(depName);
                if (depSubdir != null) {
                    dependents.computeIfAbsent(depSubdir, k -> new ArrayList<>()).add(rec.subdir());
                    inDegree.merge(rec.subdir(), 1, Integer::sum);
                } else {
                    logger.debug("Dependency '{}' declared by element at {} is not in this deployment; ignoring for ordering",
                            depName, rec.subdir());
                }
            }
        }

        // Kahn's BFS topological sort
        final var queue = new ArrayDeque<Path>();
        for (final var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        final var sorted = new ArrayList<Path>(scanRecords.size());
        while (!queue.isEmpty()) {
            final var node = queue.poll();
            sorted.add(node);
            for (final var dependent : dependents.getOrDefault(node, List.of())) {
                if (inDegree.merge(dependent, -1, Integer::sum) == 0) {
                    queue.add(dependent);
                }
            }
        }

        // Detect cycles: any subdir still in inDegree with value > 0 is part of a cycle
        if (sorted.size() < scanRecords.size()) {
            final var cycleMembers = scanRecords.stream()
                    .map(ElementScanRecord::subdir)
                    .filter(p -> !sorted.contains(p))
                    .map(Path::toString)
                    .toList();
            logger.error("@ElementDependency cycle detected among elements: {}. Appending in original order.", cycleMembers);
            scanRecords.stream()
                    .map(ElementScanRecord::subdir)
                    .filter(p -> !sorted.contains(p))
                    .forEach(sorted::add);
        }

        return sorted;

    }

    private URLClassLoader buildJarClassLoader(
            final ClassLoader parent,
            final Collection<Path> paths,
            final java.util.function.BiConsumer<Path, List<URL>> collector,
            final String type) {

        final var classpath = new ArrayList<URL>();
        final var fileSystems = new ArrayList<FileSystem>();

        for (final var path : paths) {
            try {
                // Try to open as a FileSystem (for ELM/zip files)
                final var fs = FileSystems.newFileSystem(path);
                fileSystems.add(fs); // Keep it open
                final var root = fs.getRootDirectories().iterator().next();
                collector.accept(root, classpath);
            } catch (ProviderNotFoundException ex) {
                // Not a zip/ELM file, try as directory
                if (Files.isDirectory(path)) {
                    collector.accept(path, classpath);
                } else {
                    logger.debug("{} is not a directory or ELM file. Skipping {} scan.", path, type);
                }
            } catch (final NoSuchFileException ex) {
                logger.debug("{} does not exist. Skipping {} scan.", path, type);
            } catch (final IOException ex) {
                // Close any FileSystems we've opened so far
                fileSystems.forEach(fs -> {
                    try {
                        fs.close();
                    } catch (IOException e) {
                        logger.error("Error closing FileSystem during cleanup", e);
                    }
                });
                throw new SdkException(ex);
            }
        }


        return new ElementApiClassLoader(classpath.toArray(URL[]::new), fileSystems, parent);

    }

    /**
     * Collects API jars from path/api/ subdirectory.
     * Scans immediate children of path for api/ subdirectory.
     */
    private void collectApiJars(final Path path, final List<URL> apiClasspath) {

        if (!isDirectory(path) || isPathInHiddenHierarchy(path)) {
            return;
        }

        try (final var ds = newDirectoryStream(path)) {

            // First pass: collect JARs from top-level "api" directory if it exists
            final var topLevelApi = path.resolve(API_DIR);
            if (isApiDirectory(topLevelApi)) {
                logger.info("Found top-level API directory: {}", topLevelApi);
                try (final var jarStream = newDirectoryStream(topLevelApi)) {
                    for (final var jarPath : jarStream) {
                        if (isJarFile(jarPath)) {

                            apiClasspath.add(jarPath.getFileSystem() == FileSystems.getDefault()
                                    ? toUrl(jarPath)
                                    : UrlUtils.forPath(jarPath)
                            );

                            logger.debug("Added top-level API jar: {}", jarPath);

                        }
                    }
                }
            }

            // Second pass: for each subdirectory that isn't "api", check if it contains an "api" subdirectory
            for (final var subPath : ds) {
                if (isDirectory(subPath) && !isApiDirectory(subPath)) {
                    final var elementApi = subPath.resolve(API_DIR);
                    if (isApiDirectory(elementApi)) {
                        logger.info("Found element API directory: {}", elementApi);
                        try (final var jarStream = newDirectoryStream(elementApi)) {
                            for (final var jarPath : jarStream) {
                                if (isJarFile(jarPath)) {

                                    apiClasspath.add(jarPath.getFileSystem() == FileSystems.getDefault()
                                            ? toUrl(jarPath)
                                            : UrlUtils.forPath(jarPath)
                                    );

                                    logger.debug("Added element API jar: {}", jarPath);

                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new SdkException(e);
        }
    }

    @Override
    public Optional<ClassLoader> findSpiClassLoader(final ClassLoader parent, final Path path) {

        // If somebody passed the SPI directory directly and we try there.
        if (isSpiDirectory(path)) {
            return tryBuildSpiClassLoader(parent, path);
        }

        // Otherwise, we check if they passed an element root and we try there.
        final var resolved = path.resolve(SPI_DIR);

        if (isSpiDirectory(resolved)) {
            return tryBuildSpiClassLoader(parent, resolved);
        }

        // This means the path isn't valid so we presume no SPI is specified
        return Optional.empty();

    }

    public Optional<ClassLoader> tryBuildSpiClassLoader(final ClassLoader parent, final Path path) {

        final var jars = new ArrayList<URL>();

        try (final var directory = newDirectoryStream(path)) {
            for (var subPath : directory) {
                if (isJarFile(subPath)) {
                    jars.add(toUrl(subPath));
                }
            }
        } catch (IOException ex) {
            throw new SdkException(ex);
        }

        if (jars.isEmpty()) {
            logger.warn("SPI path {} contains no artifacts.", path);
        }

        return jars.isEmpty()
                ? Optional.empty()
                : Optional.of(new URLClassLoader(
                        "SPI=%s".formatted(path),
                        jars.toArray(URL[]::new), parent)
                );

    }

    private static void closeClassLoader(final ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader ucl) {
            try {
                ucl.close();
            } catch (IOException ex) {
                logger.warn("Failed to close classloader {}", ucl.getName(), ex);
            }
        }
    }

    /**
     * Pre-scan result for a single element subdirectory.
     */
    private record ElementScanRecord(Path subdir, String elementName, List<String> dependencyNames) {}

    /**
     * Delegates to doLoadWithAttributes with a pass-through attributes provider.
     */

    private record ElementPathLoaderRecord(
            Attributes baseAttributes,
            Path elementPath,
            Path libs,
            Path classpath,
            Path attributesFile,
            ClassLoader elementParent,
            ClassLoader baseClassLoader,
            MutableElementRegistry registry,
            AttributesLoader attributesProvider) {

        public ElementPathLoaderRecord {
            elementPath = elementPath == null ? null : elementPath.toAbsolutePath();
            libs = libs == null ? null : libs.toAbsolutePath();
            classpath = classpath == null ? null : classpath.toAbsolutePath();
            attributesFile = attributesFile == null ? null : attributesFile.toAbsolutePath();
        }

        public static ElementPathLoaderRecord from(
                final Attributes baseAttributes,
                final MutableElementRegistry registry,
                final ClassLoader elementParent,
                final ClassLoader baseClassLoader,
                final Path elementPath,
                final DirectoryStream<Path> directory,
                final AttributesLoader attributesProvider) {

            Path libs, classpath, attributesFile;
            libs = classpath = attributesFile = null;

            for (var subpath : directory) {
                if (isApiDirectory(subpath)) {
                    logger.debug("Skipping API directory {} while collecting path elements.", subpath);
                } else if (isSpiDirectory(subpath)) {
                    logger.debug("Element has SPI directory {}. Will attempt to enable SPI for this Element.", subpath);
                } else if (isLibDirectory(subpath)) {
                    libs = subpath;
                } else if (isClasspathDirectory(subpath)) {
                    classpath = subpath;
                } else if (isAttributesFiles(subpath)) {
                    attributesFile = subpath;
                } else if (isPathInHiddenHierarchy(subpath)) {
                    logger.debug("Skipping hidden path: {}.", subpath);
                } else if (!isDirectory(subpath)) {
                    logger.warn("Unexpected file in Element definition: {}, ignoring.", subpath);
                } else {
                    logger.debug("Ignoring element path: {}.", subpath);
                }
            }

            return new ElementPathLoaderRecord(
                    baseAttributes,
                    elementPath,
                    libs,
                    classpath,
                    attributesFile,
                    elementParent,
                    baseClassLoader,
                    registry,
                    attributesProvider
            );

        }

        public Stream<URL> libsUrls() {
            try {
                return libs() == null
                        ? Stream.empty()
                        : list(libs())
                            .filter(DirectoryElementPathLoader::isJarFile)
                            .map(libJar -> libJar.getFileSystem() == FileSystems.getDefault()
                                    ? toUrl(libJar)
                                    : UrlUtils.forPath(libJar)
                            );
            } catch (IOException ex) {
                throw new SdkException(ex);
            }
        }

        public Stream<URL> classpathUrls() {
            return classpath() == null
                    ? Stream.empty()
                    : Stream.of(toUrl(classpath()));
        }

        public boolean isValidElement() {
            return classpath() != null || libs() != null;
        }

        public URL[] allClasspathUrls() {
            return Stream.concat(libsUrls(), classpathUrls()).toArray(URL[]::new);
        }

        public Attributes loadAttributes() {

            var builder = new SimpleAttributes.Builder();

            if (attributesFile() != null) {
                try (
                        var fis = Files.newInputStream(attributesFile());
                        var bis = new BufferedInputStream(fis)
                ) {

                    final var properties = new Properties();
                    properties.load(bis);

                    final var propertiesAttributes = PropertiesAttributes.wrap(properties);
                    builder.from(propertiesAttributes);

                } catch (IOException ex) {
                    throw new SdkException(ex);
                }
            }

            // Apply the attributes provider to allow customization

            final var attributes = builder.build();
            return attributesProvider().apply(attributes, elementPath());

        }

        public Element loadElement(final ElementPathLoader.FailedElementHandler failedElementHandler) {
            logger.debug("Loading element from: {}", elementPath());
            final var elementLoader = getLoader();
            final var elementRecord = elementLoader.getElementRecord();
            try {
                return registry().register(elementLoader);
            } catch (final Throwable t) {
                failedElementHandler.accept(elementRecord, elementPath(), t);
                throw t;
            }
        }

        private ElementLoader getLoader() {

            final var attributes = loadAttributes();

            // Build the classloader hierarchy: API -> SPI -> Implementation.
            // Create implementation classloader with SPI as parent.

            final var classLoaderName = "%s[%s]".formatted(
                    ELEMENT_PATH_ENV,
                    elementPath()
            );

            // Collect the FileSystem instances opened for lib JARs inside ZIP-based
            // filesystems (e.g. ELM archives).  They are closed explicitly when the
            // URLClassLoader is closed so we don't rely on GC for cleanup.
            final var openLibFileSystems = new ArrayList<FileSystem>();
            final var implUrls = buildImplUrls(openLibFileSystems);

            return ServiceLoader
                    .load(ElementLoaderFactory.class, baseClassLoader())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new SdkException(
                            "No SPI for " + ElementLoaderFactory.class.getName() + " " +
                            "found in framework classloader"
                    ))
                    .get()
                    .getIsolatedLoaderWithParent(
                            attributes,
                            baseAttributes,
                            baseClassLoader(),
                            cl -> new URLClassLoader(classLoaderName, implUrls, cl) {
                                @Override
                                public void close() throws IOException {
                                    try {
                                        super.close();
                                    } finally {
                                        for (final var fs : openLibFileSystems) {
                                            try {
                                                fs.close();
                                            } catch (IOException ex) {
                                                logger.warn("Failed to close lib FileSystem on classloader close", ex);
                                            }
                                        }
                                    }
                                }
                            },
                            elementParent(),
                            el -> true
                    );

        }

        /**
         * Builds the implementation classpath URL array.  For lib JARs nested inside a
         * ZIP-based {@link FileSystem} (e.g. an ELM archive), opens each JAR's own
         * {@link FileSystem} and adds it to {@code openFileSystems} so the caller can
         * close them explicitly when the classloader is closed.
         */
        private URL[] buildImplUrls(final List<FileSystem> openFileSystems) {
            try {
                final Stream<URL> libUrls = libs() == null ? Stream.empty() :
                        list(libs())
                                .filter(DirectoryElementPathLoader::isJarFile)
                                .map(libJar -> {
                                    if (libJar.getFileSystem() == FileSystems.getDefault()) {
                                        return toUrl(libJar);
                                    }
                                    try {
                                        final var fs = FileSystems.newFileSystem(libJar);
                                        openFileSystems.add(fs);
                                        return UrlUtils.forPath(libJar, fs);
                                    } catch (IOException ex) {
                                        throw new SdkException(ex);
                                    }
                                });
                return Stream.concat(libUrls, classpathUrls()).toArray(URL[]::new);
            } catch (IOException ex) {
                throw new SdkException(ex);
            }
        }

    }

}