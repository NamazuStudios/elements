package dev.getelements.elements.sdk.model.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/** Utility interface providing static methods for constructing and manipulating {@link URI} instances. */
public interface URIs {

    /**
     * Returns a {@link URI} to meet the requirements of the HTTP Origin header.
     *
     * @param uri the {@link URI} from which to derive the origin
     * @return the origin URI.
     */
    static URI originFor(final URI uri) {
        try {
            return new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                null,
                null,
                null
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Ensures that the provided {@link URI}'s path ends with a '/', then resolves the given path against it.
     *
     * @param base the base URI to append to
     * @param path the path segment to append
     * @return the resulting URI with the path appended
     */
    static URI appendPath(final URI base, final String path) {
        if (base.getPath().endsWith("/")) {
            return base.resolve(path);
        } else {
            try {
                return new URI(base.getScheme(),
                               base.getUserInfo(),
                               base.getHost(),
                               base.getPort(),
                         base.getPath()  + "/",
                               base.getQuery(),
                               base.getFragment()).resolve(path);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    /**
     * Appends multiple path segments to the given base URI by joining them with '/'.
     *
     * @param base the base URI to append to
     * @param first the first path segment to append
     * @param second the second path segment to append
     * @param additional any additional path segments to append
     * @return the resulting URI with all path segments appended
     */
    static URI appendPath(final URI base, final String first, final String second, final String ... additional) {

        final String joined = Stream.concat(
                Stream.of(first, second),
                Stream.of(additional)
        ).collect(joining("/"));

        return appendPath(base, joined);

    }

    /**
     * Replaces or appends the query string on the given base URI.
     *
     * @param base the base URI whose query string is to be replaced
     * @param query the new query string to set on the URI
     * @return the resulting URI with the query string replaced
     */
    static URI appendOrReplaceQuery(final URI base, final String query) {
        try {
            return new URI(base.getScheme(),
                    base.getUserInfo(),
                    base.getHost(),
                    base.getPort(),
                    base.getPath(),
                    query,
                    base.getFragment());
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
