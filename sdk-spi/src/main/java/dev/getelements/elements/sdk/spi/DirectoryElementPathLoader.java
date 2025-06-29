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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.*;
import static java.util.stream.Collectors.joining;

public class DirectoryElementPathLoader implements ElementPathLoader {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryElementPathLoader.class);

    @Override
    public Stream<Element> load(
            final MutableElementRegistry registry,
            final Path path,
            final ClassLoader baseClassLoader) {

        Stream<Element> result = Stream.empty();

        try (final var directory = newDirectoryStream(path)) {
            final var record = ElementPathRecord.from(registry, baseClassLoader, directory);
            final var elements = record.load();
            return elements.stream();
        } catch (IOException ex) {
            throw new SdkException(ex);
        } catch (Exception ex) {
            result.forEach(Element::close);
            throw ex;
        }

    }

    private record ElementPathRecord(
            Path libs,
            Path classpath,
            Path attributesFile,
            List<Path> directories,
            ClassLoader baseClassLoader,
            MutableElementRegistry registry,
            Element parent) {

        public static ElementPathRecord from(final MutableElementRegistry registry,
                                             final ClassLoader baseClassLoader,
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
                    registry(),
                    parent
            );

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

        private static boolean isLibDirectory(final Path path) {
            return isDirectory(path) && LIB_DIR.equals(path.getFileName().toString());
        }

        private static boolean isClasspathDirectory(final Path path) {
            return isDirectory(path) && CLASSPATH_DIR.equals(path.getFileName().toString());
        }

        private static boolean isAttributesFiles(final Path path) {
            return isRegularFile(path) && ATTRIBUTES_PROPERTIES_FILE.equals(path.getFileName().toString());
        }

        public Stream<URL> libsUrls() {
            try {
                return libs() == null
                        ? Stream.empty()
                        : list(libs())
                            .filter(ElementPathRecord::isJarFile)
                            .map(ElementPathRecord::toUrl);
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
                            parent(),
                            edr -> registry
                                    .stream()
                                    .map(element -> element.getElementRecord().definition().name())
                                    .noneMatch(name -> edr.name().equals(name))
                    );

        }

    }

}
