package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.exception.SdkElementNotFoundException;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.util.PropertiesAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
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
    public URLClassLoader buildApiClassLoader(final ClassLoader parent, final Collection<Path> paths) {
        return buildJarClassLoader(parent, paths, this::collectApiJars, "API");
    }

    @Override
    public Stream<Element> load(final LoadConfiguration config) {

        final var elements = new ArrayList<Element>();
        final var apiClassLoader = buildApiClassLoader(config.parent(), config.paths());

        try {

            // Build the list of all Elements we're trying to load
            for (final var path : config.paths()) {
                final var fromPath = loadElementsFromPath(path, apiClassLoader, config).toList();
                elements.addAll(fromPath);
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

        // Return ApiClassLoader (handles FileSystem cleanup) even if classpath is empty
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
                            apiClasspath.add(toUrl(jarPath));
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
                                    apiClasspath.add(toUrl(jarPath));
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

    /**
     * Internal method that loads Elements with custom attributes provider.
     */
    private Stream<Element> loadElementsFromPath(
            final Path path,
            final ClassLoader apiClassLoader,
            final LoadConfiguration config) {
        try {

            // Try to open as a FileSystem (for ELM/zip files)
            final var fs = FileSystems.newFileSystem(path);
            final var root = fs.getPath("/");

            final var elements = doLoadElementsFromPath(root, apiClassLoader, config).toList();

            if (elements.isEmpty()) {
                // No elements loaded, close the FileSystem immediately
                try {
                    fs.close();
                } catch (IOException e) {
                    logger.error("Error closing FileSystem for {}", path, e);
                }
                return Stream.empty();
            }

            // Attach close handlers to all elements using reference counting
            final var counter = new AtomicInteger(elements.size());

            elements.forEach(element -> element.onClose(el -> {
                if (counter.decrementAndGet() == 0) {
                    try {
                        fs.close();
                    } catch (IOException e) {
                        logger.error("Error closing FileSystem for {}", path, e);
                    }
                }
            }));

            return elements.stream();

        } catch (ProviderNotFoundException ex) {
            // Not a zip/ELM file, try as directory
            if (Files.isDirectory(path)) {
                return doLoadElementsFromPath(path, apiClassLoader, config);
            } else {
                logger.warn("{} not a directory. Skipping.", path);
                return Stream.empty();
            }
        } catch (NoSuchFileException ex) {
            logger.warn("{} not found. Skipping.", path);
            return Stream.empty();
        } catch (FileSystemAlreadyExistsException ex) {
            logger.warn("FileSystem already exists for {}. Using existing.", path);
            final var fs = FileSystems.getFileSystem(path.toUri());
            return doLoadElementsFromPath(fs.getPath("/"), apiClassLoader, config);
        } catch (IOException ex) {
            throw new SdkException(ex);
        }
    }


    /**
     * Internal method with attributes provider support.
     */
    private Stream<Element> doLoadElementsFromPath(
            final Path path,
            final ClassLoader apiClassLoader,
            final LoadConfiguration config) {

        final var elements = new ArrayList<Element>();

        try (final var directory = newDirectoryStream(path)) {

            for (final var subpath : directory) {
                if (isDirectory(subpath) && !isApiDirectory(subpath)) {
                    try (final var elementDirectory = newDirectoryStream(subpath)) {

                        // If the Element provides its own SPI, we will include it. This is not required and
                        // in that case we will assume it's on the parent provided and use the parent instead.

                        final var spiClassLoader = findSpiClassLoader(apiClassLoader, subpath)
                                .orElse(apiClassLoader);

                        final var elementClassLoader = config
                                .spiLoader()
                                .apply(spiClassLoader, subpath);

                        // Construct the record with everything needed to make the new Element

                        final var record = ElementPathRecord.from(
                                config.registry(),
                                elementClassLoader,
                                config.baseClassLoader(),
                                subpath,
                                elementDirectory,
                                config.attributesProvider()
                        );

                        // Check that the Element is valid and can be loaded. If conditions are met, then
                        // load the Element

                        if (record.isValidElement()) {
                            elements.add(record.loadElement());
                        }

                    }
                }
            }

            return elements.stream();

        } catch (IOException ex) {

            elements.forEach(el -> {
                try {
                    el.close();
                } catch (Exception suppressed) {
                    ex.addSuppressed(suppressed);
                }
            });

            throw new SdkException(ex);

        } catch (Exception ex) {

            elements.forEach(el -> {
                try {
                    el.close();
                } catch (Exception suppressed) {
                    ex.addSuppressed(suppressed);
                }
            });

            throw ex;
        }

    }

    /**
     * Delegates to doLoadWithAttributes with a pass-through attributes provider.
     */

    private record ElementPathRecord(
            Path elementPath,
            Path libs,
            Path classpath,
            Path attributesFile,
            ClassLoader elementParent,
            ClassLoader baseClassLoader,
            MutableElementRegistry registry,
            AttributesLoader attributesProvider) {

        public ElementPathRecord {
            elementPath = elementPath == null ? null : elementPath.toAbsolutePath();
            libs = libs == null ? null : libs.toAbsolutePath();
            classpath = classpath == null ? null : classpath.toAbsolutePath();
            attributesFile = attributesFile == null ? null : attributesFile.toAbsolutePath();
        }

        public static ElementPathRecord from(final MutableElementRegistry registry,
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

            return new ElementPathRecord(
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
                            .map(DirectoryElementPathLoader::toUrl);
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

            // First, load base attributes from properties file (if it exists)
            final Attributes baseAttributes;
            if (attributesFile() == null) {
                baseAttributes = Attributes.emptyAttributes();
            } else {
                try (
                        var fis = new FileInputStream(attributesFile().toFile());
                        var bis = new BufferedInputStream(fis)
                ) {
                    final var properties = new Properties();
                    properties.load(bis);
                    baseAttributes = PropertiesAttributes.wrap(properties);
                } catch (IOException ex) {
                    throw new SdkException(ex);
                }
            }

            // Apply the attributes provider to allow customization
            return attributesProvider().apply(baseAttributes, elementPath());

        }

        public Element loadElement() {

            logger.debug("Loading element from: {}", elementPath());

            try {
                final var elementLoader = getLoader();
                return registry().register(elementLoader);
            } catch (SdkElementNotFoundException ex) {
                logger.warn("Element directory {} has valid configuration but no elements were found.", elementPath(), ex);
                throw ex;
            }

        }

        private ElementLoader getLoader() {

            final var implUrls = allClasspathUrls();
            final var attributes = loadAttributes();

            // Build the classloader hierarchy: API -> SPI -> Implementation.
            // Create implementation classloader with SPI as parent

            final var classLoaderName = "%s[%s]".formatted(
                    ELEMENT_PATH_ENV,
                    elementPath()
            );

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
                            baseClassLoader(),
                            cl -> new URLClassLoader(classLoaderName, implUrls, cl),
                            elementParent(),
                            el -> true
                    );

        }

    }

}
