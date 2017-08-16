package com.namazustudios.socialengine.rt.manifest.http;

import com.namazustudios.socialengine.rt.exception.BadManifestException;

/**
 * Enumerates the standard HTTP methods.
 *
 * Created by patricktwohig on 8/15/17.
 */
public enum HttpVerb {

    /**
     * Designates the GET method.
     */
    GET,

    /**
     * Designates the POST method.
     */
    POST,

    /**
     * Designates the PUT method.
     */
    PUT,

    /**
     * Designates the DELETE method.
     */
    DELETE,

    /**
     * Designates the HEAD method.
     */
    HEAD,

    /**
     * Designates the OPTIONS method.
     */
    OPTIONS;

    /**
     * Maps the name to the {@link HttpVerb}.  Functions identically to {@link #valueOf(String)}, but
     * will throw {@link BadManifestException} if the verb is not found.
     *
     * @param name the name of the verb
     * @return the {@link HttpVerb}, never null
     */
    public static HttpVerb findByName(final String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ex) {
            throw new BadManifestException(ex);
        }
    }

}
