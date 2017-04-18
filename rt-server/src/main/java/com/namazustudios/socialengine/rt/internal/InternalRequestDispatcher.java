package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.rt.ExceptionMapper;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.ResponseReceiver;

/**
 * Objects implementing this interface are used to dispatch instances of {@link Request} to
 * instances of {@link Worker}.
 *
 *
 * Created by patricktwohig on 8/23/15.
 */
public interface InternalRequestDispatcher {

    /**
     * Dispatches the given {@link Request}.  This must ensure that the appropriate
     * {@link ExceptionMapper} is used to map any exceptions to an instance of {@link Response}.
     *
     * @param request the request
     * @param responseReceiver the receiver for the {@link Response}
     */
    void dispatch(Request request, ResponseReceiver responseReceiver);

}
