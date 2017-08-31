package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.exception.OperationNotFoundException;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.namazustudios.socialengine.rt.manifest.http.HttpVerb;

import java.util.List;

public interface HttpManifestMetadata {

    /**
     * Returns the {@link HttpManifest} associated with this request.  This must not be null.
     *
     * @return the {@link HttpManifest}
     */
    HttpManifest getManifest();

    /**
     * Returns true if this {@link HttpManifestMetadata} has a valid {@link HttpOperation}.  If this
     * returns true then {@link #getOperation()} must not throw {@link OperationNotFoundException}.
     * @return
     */
    boolean hasOperation();

    /**
     * Returns the {@link HttpOperation} associated with this request.  If no operation can
     * be found, then this will throw an instance of {@link OperationNotFoundException}.
     *
     * @return the {@link HttpOperation}
     */
    HttpOperation getOperation();

    /**
     * It is possible that the request matches may {@link HttpOperation} instances, such as when the
     * {@link HttpVerb#OPTIONS} verb is used.  This will enumerate the possible potential {@link HttpOperation}
     * instances that match the path of the request, as determined by the request URI.
     *
     * @return a list of available {@link HttpOperation}s, if any.
     */
    List<HttpOperation> getAvailableOperations();

    /**
     * Gets the requested {@link HttpContent} of this request as determined by the appropriate Content-Type headers.
     *
     * If no matching {@link HttpContent} can be determined for this request, then an instance of
     * {@link InvalidContentTypeException} will be thrown.
     *
     * @return the {@link HttpContent}
     */
    default HttpContent getContent() {
        return getContentFor(getOperation());
    }

    /**
     * Gets the requested {@link HttpContent} of this request as determined by the supplied {@link HttpOperation}.
     *
     * If no matching {@link HttpContent} can be determined for this request, then an instance of
     * {@link InvalidContentTypeException} will be thrown.
     *
     * @return the {@link HttpContent} associated, never null
     */
    HttpContent getContentFor(HttpOperation operation);

}
