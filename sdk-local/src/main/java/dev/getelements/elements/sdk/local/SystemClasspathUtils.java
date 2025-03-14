package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.exception.SdkException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

/**
 * Utilities for accessing the system classpath.
 */
public class SystemClasspathUtils {

    private SystemClasspathUtils() {}

    /**
     * Gets the System classpath as a set of URLs.
     *
     * @return the system classpath
     */
    public static URL[] getSystemClasspath() {

        final var classpath = System.getProperty("java.class.path");

        return classpath == null
                ? new URL[0]
                : Stream.of(classpath.split(File.pathSeparator))
                .map(File::new)
                .map(File::toURI)
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException ex) {
                        throw new SdkException(ex);
                    }
                })
                .toArray(URL[]::new);

    }

}
