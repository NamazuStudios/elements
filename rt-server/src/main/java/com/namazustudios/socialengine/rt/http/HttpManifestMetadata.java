package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.exception.OperationNotFoundException;
import com.namazustudios.socialengine.rt.manifest.http.*;

import java.util.List;

/**
 * This is collection of the various objects contained in the {@link HttpManifest} combined with information
 * from the {@link HttpRequest}.  This is responsible for providing the various routing information such that
 * the underlying services may instantiate {@link com.namazustudios.socialengine.rt.Resource} instances and
 * resolve the methods to invoke.
 */
public interface HttpManifestMetadata {

    /**
     * Returns the {@link HttpManifest} associated with this request.  This must not be null.
     *
     * @return the {@link HttpManifest}
     */
    HttpManifest getManifest();

    /**
     * Gets the {@link HttpModule} used to service the related {@link HttpRequest}.
     *
     * @return the {@link HttpModule}
     */
    HttpModule getModule();

    /**
     *
     * Returns true if this {@link HttpManifestMetadata} has a valid single {@link HttpOperation}.  If this
     * returns true then {@link #getPreferredOperation()} must not throw {@link OperationNotFoundException}.
     *
     * If this method returns true, then {@link #getPreferredOperation()} must return successfully and not throw
     * and instance of {@link OperationNotFoundException}.
     *
     * @return true if {@link #getPreferredOperation()} can be called without throwing an exception.
     *
     */
    boolean hasSinglePreferredOperation();

    /**
     * Returns the {@link HttpOperation} associated with this request.  If no operation can be found, then this will
     * throw an instance of {@link OperationNotFoundException}.
     *
     * If multiple {@link HttpOperation} instances apply, or no {@link HttpOperation} instances reply, then
     * this must throw any other number of exception types indicating so.
     *
     * @return the {@link HttpOperation}
     */
    HttpOperation getPreferredOperation();

    /**
     * It is possible that the request matches may {@link HttpOperation} instances, such as when the
     * {@link HttpVerb#OPTIONS} verb is used.  This will enumerate the possible potential {@link HttpOperation}
     * instances that match the attributes of the associated {@link HttpRequest}.  This may consider such
     * features as the presence of the Accept header, or other matching headers.
     *
     * @return a list of available {@link HttpOperation}s, if any.  If none match, then an empty list is returned
     */
    List<HttpOperation> getAvailableOperations();

}
