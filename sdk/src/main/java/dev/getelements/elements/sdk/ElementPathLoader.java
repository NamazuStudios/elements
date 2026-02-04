package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkException;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.lang.System.getenv;
import static java.util.List.*;

/**
 * Used to load {@link Element} instances from local paths on disk. As a single Element may span multiple sources for
 * executable code, this provides loading semantics for code on disk in a structured manner. When loading
 * {@link Element}s, the loader will search a {@link Path} with the following rules.
 *
 * <ul>
 *     <li>If the {@link Path} can load using {@link java.nio.file.FileSystems#newFileSystem(Path)}, it will load</li>
 *     <li>If the {@link Path} is a directory, each subdirectory is scanned as a potential Element</li>
 *     <li>If the {@link Path} is not found or otherwise empty, it will return an empty {@link Stream}</li>
 *     <li>Each subdirectory containing Element configuration becomes an independent {@link Element}</li>
 *     <li>Element directory names are irrelevant. All {@link Element} metadata comes from the annotations.</li>
 * </ul>
 *
 * When searching each element directory, the ElementPathLoader will look for the following files/directories to determine the
 * makings of the {@link Element}.
 *
 * <ul>
 *    <li>dev.getelements.element.attributes.properties - custom attributes for the Element</li>
 *    <li>api - every file is assumed to be a jar file, shared among all Elements in the deployment</li>
 *    <li>spi - every file is assumed to be a jar file, Service Provider Interface specific to that Element</li>
 *    <li>lib - every file is assumed to be a jar file, specific to that Element only</li>
 *    <li>classpath - every file inside this directory is added to the classpath</li>
 * </ul>
 *
 * For example, let's say we have the following deployment structure with Elements defined:
 * <ul>
 *     <li>deployment/api/ - shared API jars</li>
 *     <li>deployment/foo/ - com.example.foo Element</li>
 *     <li>deployment/bar/ - com.example.bar Element</li>
 *     <li>deployment/baz/ - com.example.baz Element</li>
 * </ul>
 *
 * This will result in three independent {@link Element} instances, each with its own classloader hierarchy:
 * API → SPI → Implementation.
 *
 * Changes for 3.6 and up. The "api" directory is now supported. Any jars in this directory are shared among all
 * Elements in the deployment. This enables each Element to share common API jars without exposing its own
 * implementation jars to its peers. API jars should be as lean as absolutely possible, containing only
 * the interfaces and data types needed to interact with the Element's services.
 *
 * Changes for 3.7 and up. The "spi" directory is now supported, and the loading model has been simplified to a flat
 * structure. Each element directory is independent with no nesting. The classloader hierarchy for each Element is:
 * API (shared) → SPI (per-element) → Implementation (per-element). The SPI layer allows developers to attach Service
 * Provider Interface jars at deployment time, enabling Elements to communicate through a common API while maintaining
 * deployment-time flexibility for their service provider implementations.
 */
public interface ElementPathLoader {

    /**
     * The jar file extension.
     */
    String JAR_EXTENSION = "jar";

    /**
     * The Element container format (elm).
     */
    String ELM_EXTENSION = "elm";

    /**
     * The mime type for the ELM files.
     */
    String ELM_MIME_TYPE = "application/zip";

    /**
     * The environment variable for the element path.
     */
    String ELEMENT_PATH_ENV = "ELEMENTPATH";

    /**
     * The API directory.
     * @since 3.6
     */
    String API_DIR = "api";

    /**
     * The SPI directory.
     * @since 3.7
     */
    String SPI_DIR = "spi";

    /**
     * The library directory name.
     */
    String LIB_DIR = "lib";

    /**
     * The Classpath directory name.
     */
    String CLASSPATH_DIR = "classpath";

    /**
     * The name of the file which represents a particular {@link Element}'s {@link Attributes}. Uses the
     * {@link java.util.Properties} format when defining the {@link Attributes}. This allows for the separation of
     * code and configuration at deployment time.
     */
    String ATTRIBUTES_PROPERTIES_FILE = "dev.getelements.element.attributes.properties";

    /**
     * Loads all {@link Element} into the supplied registry, using the globally configured ELEMENTPATH as the
     * source location.
     *
     * @param registry the registry to receive the loaded {@link Element}s
     */
    default Stream<Element> load(final MutableElementRegistry registry) {
        final var path = Path.of(getenv(ELEMENT_PATH_ENV));
        return load(registry, path);
    }

    /**
     * Loads all {@link Element}s from the supplied {@link Path} into the supplied {@link ElementRegistry}.
     * Builds an API classloader from the path and manages its lifecycle with reference counting.
     *
     * @param registry the registry to use for loading the {@link Element} instances
     * @param path the {@link Path}
     * @return a {@link Stream} of {@link Element} instances
     */
    default Stream<Element> load(final MutableElementRegistry registry, final Path path) {

        final var api = buildApiClassLoader(path).orElseGet(() -> new URLClassLoader(new URL[0],null));
        final var elements = load(registry, path, api).toList();

        // If we created an API classloader, attach close handlers to all elements for reference counting
        final var counter = new java.util.concurrent.atomic.AtomicInteger(elements.size());

        elements.forEach(element -> element.onClose(el -> {
            if (counter.decrementAndGet() == 0) {
                try {
                    api.close();
                } catch (java.io.IOException e) {
                    throw new java.io.UncheckedIOException("Error closing API classloader for " + path, e);
                }
            }
        }));

        if (elements.isEmpty()) {
            try {
                api.close();
            } catch (IOException ex) {
                throw new SdkException(ex);
            }
        }

        return elements.stream();

    }

    /**
     * Loads all {@link Element}s from the supplied {@link Path} into the supplied {@link ElementRegistry}.
     *
     * @param registry the registry to use for loading the {@link Element} instances
     * @param path the {@link Path}
     * @param baseClassLoader the base {@link ClassLoader} used to load the {@link Element}
     * @return a {@link Stream} of {@link Element} instances
     */
    Stream<Element> load(MutableElementRegistry registry, Path path, ClassLoader baseClassLoader);

    /**
     * Builds a classloader containing API jars from a collection of paths. Each path may be:
     * <ul>
     *     <li>An ELM file (zip) - scans for {@code api/} directory inside</li>
     *     <li>A directory - scans for {@code api/} subdirectory</li>
     * </ul>
     * All API jars found are combined into a single URLClassLoader with no parent (bootstrap).
     * This classloader should be passed as the baseClassLoader when loading elements to ensure
     * all elements share the same API classpath.
     * <p>
     * The returned URLClassLoader must be closed when no longer needed to release any resources
     * (such as open zip file systems for ELM files).
     *
     * @param paths collection of paths to scan for API jars
     * @return an Optional containing a URLClassLoader with all API jars, or empty if no APIs found
     * @since 3.7
     */
    Optional<URLClassLoader> buildApiClassLoader(Collection<Path> paths);

    /**
     * Builds a classloader containing API jars from one or more paths.
     * Convenience method that delegates to {@link #buildApiClassLoader(java.util.Collection)}.
     * <p>
     * The returned URLClassLoader must be closed when no longer needed.
     *
     * @param paths one or more paths to scan for API jars
     * @return an Optional containing a URLClassLoader with all API jars, or empty if no APIs found
     * @since 3.7
     */
    default Optional<URLClassLoader> buildApiClassLoader(final Path... paths) {
        return buildApiClassLoader(of(paths));
    }

    /**
     * Creates a new instance of the {@link ElementPathLoader} using the system default SPI.
     *
     * @return new {@link ElementPathLoader}
     */
    static ElementPathLoader newDefaultInstance() {
        return ServiceLoader
                .load(ElementPathLoader.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("No ElementPathLoader SPI Available."))
                .get();
    }

}
