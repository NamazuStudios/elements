package dev.getelements.elements.sdk.local.internal;

import dev.getelements.elements.sdk.exception.SdkException;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Classpath utilities. Used to parse the classpath and other related operations.
 */
public class ClasspathUtils {

    public static List<URL> parse(final String classpath) {

        final var urls = new ArrayList<URL>();
        final var separator = System.getProperty("path.separator");
        final var paths = classpath.split(separator);

        try {
            for (var path : paths) {
                if (path.endsWith("*")) {
                    // Handle wildcard for JARs in a directory
                    final var dir = new File(path.substring(0, path.length() - 1));
                    if (dir.isDirectory()) {
                        final var jarFiles = dir.listFiles((file) -> file.isFile() && file.getName().endsWith(".jar"));
                        if (jarFiles != null) {
                            for (File jarFile : jarFiles) {
                                urls.add(jarFile.toURI().toURL());
                            }
                        }
                    }
                } else {
                    final var file = new File(path);
                    if (file.exists()) {
                        urls.add(file.toURI().toURL());
                    }
                }
            }

            return urls;

        } catch (Exception ex) {
            throw new SdkException(ex);
        }

    }

}
