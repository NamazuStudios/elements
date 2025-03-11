package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.exception.SdkException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;
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
