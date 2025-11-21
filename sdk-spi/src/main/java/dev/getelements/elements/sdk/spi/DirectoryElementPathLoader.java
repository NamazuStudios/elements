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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static java.nio.file.Files.*;
import static java.util.stream.Collectors.joining;

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
    public Stream<Element> load(
            final MutableElementRegistry registry,
            final Path path,
            final ClassLoader baseClassLoader) {
        try (final var fs = FileSystems.newFileSystem(path)) {
            final var root = fs.getPath("/");
            return doLoad(registry, root, baseClassLoader);
        } catch (ProviderNotFoundException ex) {
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

        final var sharedClassLoader = SharedClasspathRecord
                .from(path)
                .build()
                .newClassLoader();

        return doLoadWithSharedClasspath(registry, path, baseClassLoader, sharedClassLoader);

    }

    private Stream<Element> doLoadWithSharedClasspath(
            final MutableElementRegistry registry,
            final Path path,
            final ClassLoader baseClassLoader,
            final ClassLoader sharedClassloader) {

        Stream<Element> result = Stream.empty();

        try (final var directory = newDirectoryStream(path)) {
            final var record = ElementPathRecord.from(registry, baseClassLoader, sharedClassloader, directory);
            final var elements = record.load();
            return elements.stream();
        } catch (IOException ex) {
            throw new SdkException(ex);
        } catch (Exception ex) {
            result.forEach(Element::close);
            throw ex;
        }

    }

    private record SharedClasspathRecord(
            Path path,
            List<URL> classpath
    ) {

        public static SharedClasspathRecord from(final Path path) {
            return new SharedClasspathRecord(path, new ArrayList<>());
        }

        public SharedClasspathRecord build() {
            build(path());
            return this;
        }

        public ClassLoader newClassLoader() {
            return new URLClassLoader(classpath.toArray(URL[]::new), null);
        }

        private void build(final Path path) {
            if (isLibDirectory(path)) {
                logger.trace("Not including library directory {} in shared classpath.", path);
            } else if (isClasspathDirectory(path)) {
                logger.trace("Not including classpath directory {} in shared classpath.", path);
            } else if (isAttributesFiles(path)) {
                logger.trace("Skipping attributes file {} while evaluating shared classpath.", path);
            } else if (isApiDirectory(path)) {

                logger.info("Including API directory {} in shared classpath.", path);

                try (final var ds = newDirectoryStream(path)) {
                    for (final var subPath : ds) {
                        if (isJarFile(subPath)) {
                            classpath.add(toUrl(subPath));
                            logger.debug("Added {} to shared classpath.", subPath);
                        }
                    }
                } catch (IOException e) {
                    throw new SdkException(e);
                }

            } else if (isDirectory(path)) {
                try (final var ds = newDirectoryStream(path)) {
                    for (final var subPath : ds) {
                        build(subPath);
                    }
                } catch (IOException e) {
                    throw new SdkException(e);
                }
            } else {
                logger.info("Skipping {} while evaluating shared classpath. Not a directory.", path);
            }
        }

    }

    private record ElementPathRecord(
            Path libs,
            Path classpath,
            Path attributesFile,
            List<Path> directories,
            ClassLoader baseClassLoader,
            ClassLoader sharedClassLoader,
            MutableElementRegistry registry,
            Element parent) {

        public static ElementPathRecord from(final MutableElementRegistry registry,
                                             final ClassLoader baseClassLoader,
                                             final ClassLoader sharedClassLoader,
                                             final DirectoryStream<Path> directory) {

            Path libs, classpath, attributesFile;
            libs = classpath = attributesFile = null;

            final var directories = new ArrayList<Path>();

            for (var subpath : directory) {
                if (isLibDirectory(subpath)) {
                    libs = subpath;
                } else if (isClasspathDirectory(subpath)) {
                    classpath = subpath;
                } else if (isAttributesFiles(subpath)) {
                    attributesFile = subpath;
                } else if (isDirectory(subpath)) {
                    directories.add(subpath);
                } else if (isPathInHiddenHierarchy(subpath)) {
                    logger.debug("Skipping hidden path: {}.", subpath);
                } else {
                    logger.warn("Unexpected path in Element definition: {}, ignoring.", subpath);
                }
            }

            return new ElementPathRecord(
                    libs,
                    classpath,
                    attributesFile,
                    List.copyOf(directories),
                    baseClassLoader,
                    sharedClassLoader,
                    registry,
                    null
            );

        }

        public ElementPathRecord descendant(final Element parent, final DirectoryStream<Path> directory) {

            Path libs, classpath, attributesFile;
            libs = classpath = attributesFile = null;

            final var directories = new ArrayList<Path>();

            for (var subpath : directory) {
                if (isLibDirectory(subpath)) {
                    libs = subpath;
                } else if (isClasspathDirectory(subpath)) {
                    classpath = subpath;
                } else if (isAttributesFiles(subpath)) {
                    attributesFile = subpath;
                } else if (isDirectory(subpath)) {
                    directories.add(subpath);
                } else if (isPathInHiddenHierarchy(subpath)) {
                    logger.debug("Skipping hidden path: {}.", subpath);
                } else {
                    logger.warn("Unexpected path in Element definition: {}, ignoring.", subpath);
                }
            }

            return new ElementPathRecord(
                    libs,
                    classpath,
                    attributesFile,
                    directories,
                    baseClassLoader(),
                    sharedClassLoader(),
                    registry(),
                    parent
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

        public boolean isValidClasspath() {
            return classpath() != null || libs() != null;
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

        public List<Element> load() {

            final var results = new ArrayList<Element>();

            Element element = parent();

            if (isValidClasspath()) {

                logger.debug("Loading classpath: {}", directories());

                try {
                    final var elementLoader = getLoader();
                    element = registry().register(elementLoader);
                    results.add(element);
                } catch (SdkElementNotFoundException ex) {
                    logger.warn(
                            "{} has valid classpath but no elements were found.",
                            directories().stream().map(Path::toString).collect(joining(",")),
                            ex);
                }
            } else {
                logger.debug("Skipping load of elements from {}", directories());
            }

            for (var subDirectory : directories()) {
                try (final var subDirectoryStream = newDirectoryStream(subDirectory)) {
                    final var descendant = descendant(element, subDirectoryStream);
                    results.addAll(descendant.load());
                } catch (IOException ex) {
                    throw new SdkException(ex);
                } catch (Exception ex) {
                    results.forEach(Element::close);
                    throw ex;
                }
            }

            return results;

        }

        private ElementLoader getLoader() {

            final var urls = allClasspathUrls();
            final var attributes = loadAttributes();

            final var classLoaderName = String.format("%s={%s}",
                    ELEMENT_PATH_ENV,
                    Stream
                            .of(urls)
                            .map(URL::toString)
                            .collect(joining())
            );

            return ServiceLoader
                    .load(ElementLoaderFactory.class, baseClassLoader)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new SdkException(
                            "No SPI for " + ElementLoaderFactory.class.getName() + " " +
                            "found in " + baseClassLoader.getName()
                    ))
                    .get()
                    .getIsolatedLoaderWithParent(
                            attributes,
                            baseClassLoader,
                            cl -> new URLClassLoader(classLoaderName, urls, cl),
                            parent() == null
                                    ? sharedClassLoader()
                                    : parent().getElementRecord().classLoader(),
                            edr -> registry
                                    .stream()
                                    .map(element -> element.getElementRecord().definition().name())
                                    .noneMatch(name -> edr.name().equals(name))
                    );

        }

    }

}
