package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkException;

import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static java.lang.System.getenv;

/**
 * Used to load {@link Element} instances from local paths on disk. As a single Element may span multiple sources for
 * executable code, this provides loading semantics for code on disk in a structured manner. When loading
 * {@link Element}s, the loader will search a {@link Path} with the following rules.
 *
 * <ul>
 *     <li>If the {@link Path} is a file, it will assume that the entire {@link Element} is in one file.</li>
 *     <li>If thd {@link Path} is a directory, it will assume all files in the directory make up the {@link Element}</li>
 *     <li>For each directory in the {@link Path}, it will create a new subordinate {@link ElementRegistry}</li>
 *     <li>Each loaded {@link Element} will follow the hierarchy of the directory.</li>
 *     <li>Empty directories, or directories containing directories will be skipped.</li>
 *     <li>Aside from those specified (lib and classpath), directory names are irrelevant. All {@link Element} metadata will come from the annotations.</li>
 * </ul>
 *
 * When searching each directory, the ElementPathLoader will look for the following files/directories to determine the
 * makings of the {@link Element}.
 *
 * <ul>
 *    <li>dev.getelements.element.attributes.properties - custom attributes for the Element</li>
 *    <li>lib - every file is assumed to be a jar file</li>
 *    <li>classpath - every file inside this directory is added to the classpath</li>
 * </ul>
 *
 * For example, let's say we have the following directories with the following Elements define inside each of them:
 * <ul>
 *     <li>foo - com.example.foo</li>
 *     <li>foo/a - com.example.foo.a</li>
 *     <li>foo/b - com.example.foo.b</li>
 *     <li>bar - com.example.bar</li>
 *     <li>bar/a - com.example.bar.a</li>
 *     <li>bar/b - com.example.bar.b</li>
 * </ul>
 *
 * This will result in six {@link ElementRegistry} instances and {@link Element} instances chained as follows:
 *
 * <ul>
 *     <li>Root</li>
 *     <li>Root -> Foo</li>
 *     <li>Root -> Foo -> A</li>
 *     <li>Root -> Foo -> B</li>
 *     <li>Root</li>
 *     <li>Root -> Bar</li>
 *     <li>Root -> Bar -> A</li>
 *     <li>Root -> Bar -> B</li>
 * </ul>
 *
 */
public interface ElementPathLoader {

    /**
     * The jar file extension.
     */
    String JAR_EXTENSION = "jar";

    /**
     * The environment variable for the element path.
     */
    String ELEMENT_PATH_ENV = "ELEMENTPATH";

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
        final var systemClassLoader = getClass().getClassLoader();
        return load(registry, path, systemClassLoader);
    }

    /**
     * Loads all {@link Element}s from the supplied {@link Path} into the supplied {@link ElementRegistry}.
     *
     * @param registry the registry to use for loading the {@link Element} instances
     * @param path the {@link Path}
     * @return a {@link Stream} of {@link Element} instances
     */
    default Stream<Element> load(final MutableElementRegistry registry, final Path path) {
        return load(registry, path, getClass().getClassLoader());
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
