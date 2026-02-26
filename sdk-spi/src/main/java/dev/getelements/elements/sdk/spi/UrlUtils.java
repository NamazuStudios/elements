package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.exception.SdkException;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.UUID;
import java.util.stream.Stream;

public class UrlUtils {

    private UrlUtils() {}

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

        class FileSystemInputStream extends FilterInputStream {

            private final FileSystem fileSystem;

            FileSystemInputStream(final InputStream in, final FileSystem fileSystem) {
                super(in);
                this.fileSystem = fileSystem;
            }

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    fileSystem.close();
                }
            }
        }

        try {

            final var uuid = UUID.randomUUID();
            final var host = "%016X%016X".formatted(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
            final var uri = URI.create("elm://" + host + "/");

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

                            final var resourcePath = urlPath.substring(1);
                            final var innerFs = FileSystems.newFileSystem(jarPath);
                            final var root = innerFs.getRootDirectories().iterator().next();

                            return new FileSystemInputStream(
                                    Files.newInputStream(root.resolve(resourcePath)),
                                    innerFs
                            );

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
