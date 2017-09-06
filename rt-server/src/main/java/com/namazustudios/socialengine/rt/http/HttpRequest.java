package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpVerb;

/**
 * A type of {@link Request} that contains specific manifest metadata such as the original
 * {@link com.namazustudios.socialengine.rt.manifest.http.HttpManifest}.  In general, implementations
 * type should defer throwing exceptions as long as possible (including at construction time).
 */
public interface HttpRequest extends Request {

    /**
     * Gets the {@link HttpVerb} from this {@link HttpRequest}.
     *
     * @return the {@link HttpVerb}
     */
    HttpVerb getVerb();

    /**
     * Returns the known {@link HttpManifestMetadata} for this {@link Request}.  If the appropraite
     * manifest metadata can't be found, then this may throw the appropriate exception type.
     *
     * This must throw an exception if the {@link HttpManifest} cannot be loaded.
     *
     * @return the {@link HttpManifestMetadata} for this {@link Request}
     */
    HttpManifestMetadata getManifestMetadata();

    /**
     * Gets the {@link HttpContent} for a {@link HttpResponse} which matches this request's content.  If
     * no matching {@link HttpContent} can be found for this request, then an instance of
     * {@link InvalidContentTypeException} must be thrown.
     *
     * @return the {@link HttpContent}
     *
     * @throws {@link BadRequestException} if there is not suitable content type available.
     */
    HttpContent getResponseContent();

}
