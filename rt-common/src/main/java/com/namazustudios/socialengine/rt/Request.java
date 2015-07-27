package com.namazustudios.socialengine.rt;

/**
 * Ties the {@link RequestHeader} to a specific payload type for
 * the application.
 *
 * Created by patricktwohig on 7/26/15.
 */
public interface Request<PayloadT> {

    RequestHeader getHeader();

    PayloadT getPayload();

}
