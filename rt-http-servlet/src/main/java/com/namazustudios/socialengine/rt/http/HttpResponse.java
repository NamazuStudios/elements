package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

public interface HttpResponse {

    /**
     * Returns the {@link HttpManifest} associated with this response.
     *
     * @return the {@link HttpManifest}
     */
    HttpManifest getManifest();

    /**
     * Returns the {@link HttpOperation} associated with this response.
     *
     * @return the {@link HttpOperation}
     */
    HttpOperation getOperation();

    /**
     * Gets the requested {@link HttpContent} of this request as determined
     * by the appropriate Accepts headers.
     *
     * @return the {@link HttpContent}
     */
    HttpContent getContent();

}
