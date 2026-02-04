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
            return path.toUri().toURL();
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
    public Optional<URLClassLoader> buildApiClassLoader(final Collection<Path> paths) {
        final var apiClasspath = new ArrayList<URL>();
        final var fileSystems = new ArrayList<FileSystem>();

        for (final var path : paths) {
            try {
                // Try to open as a FileSystem (for ELM/zip files)
                final var fs = FileSystems.newFileSystem(path);
                fileSystems.add(fs); // Keep it open
                final var root = fs.getPath("/");
                collectApiJars(root, apiClasspath);
            } catch (ProviderNotFoundException ex) {
                // Not a zip/ELM file, try as directory
                if (Files.isDirectory(path)) {
                    collectApiJars(path, apiClasspath);
                } else {
                    logger.debug("{} is not a directory or ELM file. Skipping API scan.", path);
                }
            } catch (final NoSuchFileException ex) {
                logger.debug("{} does not exist. Skipping API scan.", path);
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

        if (apiClasspath.isEmpty()) {
            // No APIs found, close any FileSystems we opened
            fileSystems.forEach(fs -> {
                try {
                    fs.close();
                } catch (IOException e) {
                    logger.error("Error closing FileSystem", e);
                }
            });
            return Optional.empty();
        }

        // Return ApiClassLoader which will close the FileSystems when it's closed
        return Optional.of(new ApiClassLoader(apiClasspath.toArray(URL[]::new), fileSystems));
    }

    @Override
    public Stream<Element> load(
            final MutableElementRegistry registry,
            final Path path,
            final ClassLoader baseClassLoader) {
        try {

            // Try to open as a FileSystem (for ELM/zip files)
            final var fs = FileSystems.newFileSystem(path);
            final var root = fs.getPath("/");
            final var elements = doLoad(registry, root, baseClassLoader).toList();

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
            final var counter = new java.util.concurrent.atomic.AtomicInteger(elements.size());

            elements.forEach(element -> element.onClose(el -> {
                if (counter.decrementAndGet() == 0) {
                    try {
                        fs.close();
                        logger.debug("Closed FileSystem for {} after all elements closed", path);
                    } catch (IOException e) {
                        logger.error("Error closing FileSystem for {}", path, e);
                    }
                }
            }));

            return elements.stream();

        } catch (ProviderNotFoundException ex) {
            // Not a zip/ELM file, try as directory
            if (Files.isDirectory(path)) {
                return doLoad(registry, path, baseClassLoader);
            } else {
                logger.warn("{} not a directory. Skipping.", path);
                return Stream.empty();
            }
        } catch (final NoSuchFileException ex) {
            logger.warn("{} does not exist. Skipping.", path);
            return Stream.empty();
        } catch (final IOException ex) {
            throw new SdkException(ex);
        }

    }

    private Stream<Element> doLoad(
            final MutableElementRegistry registry,
            final Path path,
            final ClassLoader baseClassLoader) {

        final var elements = new ArrayList<Element>();

        try (final var directory = newDirectoryStream(path)) {

            for (final var subpath : directory) {
                if (isDirectory(subpath)) {
                    try (final var elementDirectory = newDirectoryStream(subpath)) {
                        final var record = ElementPathRecord.from(registry, baseClassLoader, subpath, elementDirectory);
                        if (record.isValidElement()) {
                            elements.add(record.loadElement());
                        }
                    }
                }
            }

            return elements.stream();

        } catch (IOException ex) {
            elements.forEach(Element::close);
            throw new SdkException(ex);
        } catch (Exception ex) {
            elements.forEach(Element::close);
            throw ex;
        }

    }

    private void collectApiJars(final Path path, final List<URL> apiClasspath) {
        collectApiJarsRecursive(path, apiClasspath);
    }

    private void collectApiJarsRecursive(final Path path, final List<URL> apiClasspath) {
        if (isSpiDirectory(path) || isLibDirectory(path) || isClasspathDirectory(path) || isAttributesFiles(path)) {
            logger.debug("Skipping implementation path {} while collectin API jars.", path);
        } else if (isPathInHiddenHierarchy(path)) {
            logger.debug("Skipping hidden path {} while collecting API jars.", path);
        } else if (isApiDirectory(path)) {
            logger.info("Found API directory: {}", path);
            try (final var ds = newDirectoryStream(path)) {
                for (final var subPath : ds) {
                    if (isJarFile(subPath)) {
                        apiClasspath.add(toUrl(subPath));
                        logger.debug("Added API jar: {}", subPath);
                    }
                }
            } catch (IOException e) {
                throw new SdkException(e);
            }
        } else if (isDirectory(path)) {
            try (final var ds = newDirectoryStream(path)) {
                for (final var subPath : ds) {
                    collectApiJarsRecursive(subPath, apiClasspath);
                }
            } catch (IOException e) {
                throw new SdkException(e);
            }
        }
    }

    private record ElementPathRecord(
            Path elementPath,
            Path spi,
            Path libs,
            Path classpath,
            Path attributesFile,
            ClassLoader baseClassLoader,
            MutableElementRegistry registry) {

        public static ElementPathRecord from(final MutableElementRegistry registry,
                                             final ClassLoader baseClassLoader,
                                             final Path elementPath,
                                             final DirectoryStream<Path> directory) {

            Path spi, libs, classpath, attributesFile;
            spi = libs = classpath = attributesFile = null;

            for (var subpath : directory) {
                if (isApiDirectory(subpath)) {
                    logger.debug("Skipping API directory {} while collecting path elements.", subpath);
                } else if (isSpiDirectory(subpath)) {
                    spi = subpath;
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
                    spi,
                    libs,
                    classpath,
                    attributesFile,
                    baseClassLoader,
                    registry
            );

        }

        public Stream<URL> spiUrls() {
            try {
                return spi() == null
                        ? Stream.empty()
                        : list(spi())
                            .filter(DirectoryElementPathLoader::isJarFile)
                            .map(DirectoryElementPathLoader::toUrl);
            } catch (IOException ex) {
                throw new SdkException(ex);
            }
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
            return spi() != null || classpath() != null || libs() != null;
        }

        public URL[] allClasspathUrls() {
            return Stream.concat(libsUrls(), classpathUrls()).toArray(URL[]::new);
        }

        public Attributes loadAttributes() {

            if (attributesFile() == null) {
                return Attributes.emptyAttributes();
            }

            try (
                    var fis = new FileInputStream(attributesFile().toFile());
                    var bis = new BufferedInputStream(fis)
            ) {
                final var properties = new Properties();
                properties.load(bis);
                return PropertiesAttributes.wrap(properties);
            } catch (IOException ex) {
                throw new SdkException(ex);
            }

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

            final var spiUrls = spiUrls().toArray(URL[]::new);
            final var implUrls = allClasspathUrls();
            final var attributes = loadAttributes();

            // Build the classloader hierarchy: API -> SPI -> Implementation
            // The baseClassLoader is the shared API classloader (or system classloader)

            // Create SPI classloader if there are SPI jars
            final var spiClassLoader = spiUrls.length > 0
                    ? new URLClassLoader(
                            String.format("%s-spi[%s]", ELEMENT_PATH_ENV, elementPath().getFileName()),
                            spiUrls,
                            baseClassLoader)
                    : baseClassLoader;

            // Create implementation classloader with SPI as parent
            final var classLoaderName = String.format("%s[%s]",
                    ELEMENT_PATH_ENV,
                    elementPath().getFileName()
            );

            // Load ElementLoaderFactory from the framework classloader (where sdk-spi is loaded)
            final var frameworkClassLoader = DirectoryElementPathLoader.class.getClassLoader();

            return ServiceLoader
                    .load(ElementLoaderFactory.class, frameworkClassLoader)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new SdkException(
                            "No SPI for " + ElementLoaderFactory.class.getName() + " " +
                            "found in framework classloader"
                    ))
                    .get()
                    .getIsolatedLoaderWithParent(
                            attributes,
                            baseClassLoader,
                            cl -> new URLClassLoader(classLoaderName, implUrls, cl),
                            spiClassLoader,
                            edr -> registry
                                    .stream()
                                    .map(element -> element.getElementRecord().definition().name())
                                    .noneMatch(name -> edr.name().equals(name))
                    );

        }

    }

}
