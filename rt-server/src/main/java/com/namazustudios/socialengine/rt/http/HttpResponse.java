package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Response;

/**
 *
 */
public interface HttpResponse extends Response {

    /**
     * Returns the known {@link HttpManifestMetadata} for this {@link Request}.  If the appropriate metadata
     * manifest metadata can't be found, then this may throw the appropriate exception type.
     *
     * @return the {@link HttpManifestMetadata} for this {@link Response}
     */
    HttpManifestMetadata getManifestMetadata();

}
