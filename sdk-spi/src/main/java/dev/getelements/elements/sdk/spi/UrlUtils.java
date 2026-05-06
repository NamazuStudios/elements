package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.exception.SdkException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Cleaner;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.UUID;
import java.util.stream.Stream;

public class UrlUtils {

    private UrlUtils() {}

    /** Closes {@link java.nio.file.FileSystem} instances when their associated URL handler is GC'd. */
    private static final Cleaner FILE_SYSTEM_CLEANER = Cleaner.create();

    /**
     * Converts the supplied string to a URL which will load its authority segment as text.
     *
     * @param uriString the uri string
     * @return the URL
     */
    public static URL toUrl(final String uriString) {
        try {

            final var uri = new URI(uriString);

            if (uri.getScheme() == null || !uri.getScheme().equals("text")) {
                throw new SdkException("Invalid URI (expected text): " + uriString);
            }

            return URL.of(uri, new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(final URL url) throws IOException {
                    return new URLConnection(url) {
                        @Override
                        public void connect(){}

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return new ByteArrayInputStream(uri.getAuthority().getBytes());
                        }
                    };
                }
            });

        } catch (MalformedURLException | URISyntaxException ex) {
            throw new SdkException(ex);
        }
    }

    /**
     * Creates a URL using the {@code elm} scheme for a {@link Path} that is a JAR file nested
     * inside a ZIP-based {@link java.nio.file.FileSystem} (e.g., a lib JAR inside an .elm archive).
     * The returned URL uses a custom {@link URLStreamHandler} that opens the nested JAR as its own
     * NIO {@link java.nio.file.FileSystem} and delegates all resource lookups to NIO {@link Files}
     * operations, making it transparent to {@link java.net.URLClassLoader} whether the JAR lives
     * on disk or inside a ZIP.
     *
     * @param jarPath path to a JAR file within an already-open ZIP FileSystem
     * @return a URL suitable for use as a URLClassLoader classpath entry
     */
    public static URL forPath(final Path jarPath) {

        try {

            final var uuid = UUID.randomUUID();
            final var host = "%016X%016X".formatted(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
            final var uri = URI.create("elm://" + host + "/");
            final var innerFs = FileSystems.newFileSystem(jarPath);
            final var fsRoot = innerFs.getRootDirectories().iterator().next();

            // Open the nested JAR's FileSystem once and reuse it for every resource lookup.
            // Previously a new FileSystem was opened (and the entire JAR decompressed from the ELM
            // archive) on every getInputStream() call, causing ~45s startup time as Jersey made
            // hundreds of resource lookups across all bundled JARs.
            final var handler = new URLStreamHandler() {

                @Override
                protected URLConnection openConnection(final URL url) {

                    return new URLConnection(url) {

                        @Override
                        public void connect() {}

                        @Override
                        public InputStream getInputStream() throws IOException {

                            final var urlPath = url.getPath();

                            if (urlPath.isEmpty() || urlPath.equals("/")) {
                                return InputStream.nullInputStream();
                            }

                            return Files.newInputStream(fsRoot.resolve(urlPath.substring(1)));
                        }
                    };
                }
            };

            // Close the FileSystem when the handler (and therefore the URL) is GC'd.
            // The action captures only innerFs — NOT handler — so the Cleaner can detect
            // when handler becomes phantom-reachable.
            FILE_SYSTEM_CLEANER.register(handler, () -> {
                try {
                    innerFs.close();
                } catch (IOException ignored) {}
            });

            return URL.of(uri, handler);

        } catch (IOException ex) {
            throw new SdkException(ex);
        }
    }

    /**
     * Creates a URL for a JAR file nested inside a ZIP-based {@link java.nio.file.FileSystem}
     * using an already-open {@link java.nio.file.FileSystem}.  The caller is responsible for
     * closing {@code fs} when the URL (and its associated {@link java.net.URLClassLoader}) is
     * no longer needed.  Use this overload when you need deterministic, explicit cleanup;
     * use {@link #forPath(Path)} when you want automatic GC-based cleanup instead.
     *
     * @param jarPath path to a JAR file within {@code fs}
     * @param fs      an already-open FileSystem wrapping the JAR; the caller owns its lifecycle
     * @return a URL suitable for use as a URLClassLoader classpath entry
     */
    public static URL forPath(final Path jarPath, final java.nio.file.FileSystem fs) {
        try {
            final var uuid = UUID.randomUUID();
            final var host = "%016X%016X".formatted(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
            final var uri = URI.create("elm://" + host + "/");
            final var fsRoot = fs.getRootDirectories().iterator().next();

            return URL.of(uri, new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(final URL url) {
                    return new URLConnection(url) {
                        @Override
                        public void connect() {}

                        @Override
                        public InputStream getInputStream() throws IOException {
                            final var urlPath = url.getPath();
                            if (urlPath.isEmpty() || urlPath.equals("/")) {
                                return InputStream.nullInputStream();
                            }
                            return Files.newInputStream(fsRoot.resolve(urlPath.substring(1)));
                        }
                    };
                }
            });
        } catch (MalformedURLException ex) {
            throw new SdkException(ex);
        }
    }

    /**
     * Converts the supplied list of strings to URLs.
     *
     * @param uriStrings the URI strings
     * @return an {@link Enumeration} of URLs
     */
    public static Enumeration<URL> toUrls(final String ... uriStrings) {

        final var iterator = Stream
                .of(uriStrings)
                .map(UrlUtils::toUrl)
                .iterator();

        return new Enumeration<>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }

        };
    }

}
