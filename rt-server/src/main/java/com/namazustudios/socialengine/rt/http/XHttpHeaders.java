package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.ResponseHeader;

public interface XHttpHeaders {

    /**
     * When used with {@link HttpRequest} and {@link HttpResponse}, this indicates the value to be returned by
     * {@link RequestHeader#getSequence()} as well as {@link ResponseHeader#getSequence()}
     */
    String RT_SEQUENCE = "X-NamazuRequestSequence";

    /**
     * When used with {@link HttpResponse}, this indicates the value to be returned with
     * {@link ResponseHeader#getCode()}.
     */
    String RT_RESPONSE_CODE = "X-NamazuResponseCode";

    /**
     * Used by "X-HTTP-Method-Override"
     */
    String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

}
