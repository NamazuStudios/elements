package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

/**
 * A type of {@link Request} that contains additional routing information specific to HTTP.
 */
public interface HttpRequest extends Request {

    /**
     * Returns the {@link HttpManifest} associated with this request.
     *
     * @return the {@link HttpManifest}
     */
    HttpManifest getManifest();

    /**
     * Returns the {@link HttpOperation} associated with this request.
     *
     * @return the {@link HttpOperation}
     */
    HttpOperation getOperation();

    /**
     * Gets the requested {@link HttpContent} of this request as determined
     * by the appropriate Content-Type headers.
     *
     * @return the {@link HttpContent}
     */
    HttpContent getContent();

}
