package dev.getelements.elements.sdk.model.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Created by patricktwohig on 8/23/17.
 */
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
     * Ensures that the provided {@link URI}'s {@link URI#getPath()} ends with a '/' thus indicating
     * @param base
     * @return
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

    static URI appendPath(final URI base, final String first, final String second, final String ... additional) {

        final String joined = Stream.concat(
                Stream.of(first, second),
                Stream.of(additional)
        ).collect(joining("/"));

        return appendPath(base, joined);

    }

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
