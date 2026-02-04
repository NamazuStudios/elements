package dev.getelements.elements.sdk.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * A URLClassLoader that manages the lifecycle of FileSystem instances opened for ELM files.
 * When this classloader is closed, it also closes all associated FileSystems to release resources.
 */
public class ApiClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ApiClassLoader.class);

    private final List<FileSystem> fileSystems;

    /**
     * Creates a new ApiClassLoader with the specified URLs, FileSystems, and parent ClassLoader.
     *
     * @param urls the URLs from which to load classes and resources
     * @param fileSystems the FileSystems to close when this classloader is closed
     * @param parent the parent ClassLoader, or null to use the bootstrap classloader
     */
    public ApiClassLoader(final URL[] urls, final List<FileSystem> fileSystems, final ClassLoader parent) {

        super("API=[%s]".formatted(
                Stream.concat(
                        Stream.of(urls).map(URL::toString),
                        fileSystems.stream().map("fs:%s"::formatted)
                ).collect(joining(","))
        ), urls, parent);

        this.fileSystems = fileSystems != null ? List.copyOf(fileSystems) : List.of();

    }

    /**
     * Closes this classloader and all associated FileSystems.
     * Continues closing all FileSystems even if some fail to close.
     */
    @Override
    public void close() throws IOException {
        IOException firstException = null;

        // First close the URLClassLoader
        try {
            super.close();
        } catch (IOException ex) {
            firstException = ex;
            logger.error("Error closing URLClassLoader", ex);
        }

        // Then close all FileSystems, collecting any exceptions
        final var exceptions = new ArrayList<IOException>();
        for (final var fs : fileSystems) {
            try {
                fs.close();
            } catch (IOException ex) {
                exceptions.add(ex);
                logger.error("Error closing FileSystem: {}", fs, ex);
            }
        }

        // Throw the first exception if any occurred
        if (firstException != null) {
            exceptions.forEach(firstException::addSuppressed);
            throw firstException;
        } else if (!exceptions.isEmpty()) {
            final var ex = exceptions.get(0);
            exceptions.stream().skip(1).forEach(ex::addSuppressed);
            throw ex;
        }
    }

}
