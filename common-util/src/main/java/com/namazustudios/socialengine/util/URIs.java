package com.namazustudios.socialengine.util;

import com.google.common.base.Joiner;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by patricktwohig on 8/23/17.
 */
public interface URIs {

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
        final String joined = Joiner.on("/").join(first, second, additional);
        return appendPath(base, joined);
    }

    static URI appendOrReplaceQuery(final URI base, final String query) {
        try {
            return new URI(base.getScheme(),
                    base.getUserInfo(),
                    base.getHost(),
                    base.getPort(),
                    base.getPath()  + "/",
                    query,
                    base.getFragment());
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
