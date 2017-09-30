package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.http.HttpRequest;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

/**
 * This translates the {@link HttpServletRequest} instances to {@link Request} which can be passed to the
 * underlying handlers.
 */
public interface HttpRequestService {

    /**
     * Gets the {@link HttpRequest} from the provided {@link HttpServletRequest}.  THis does not consider the async
     * status of the {@link HttpServletRequest}.
     *
     * @param req the {@link HttpServletRequest}
     * @return the {@link HttpServletRequest}
     */
    HttpRequest getRequest(final HttpServletRequest req);

    /**
     * Starts an {@link AsyncContext} from the provided {@link HttpServletRequest} if it is not started and derives an
     * {@link HttpRequest} from it.
     *
     * @param req the {@link HttpServletRequest}
     * @return the {@link HttpRequest}
     */
    default HttpRequest getAsyncRequest(final HttpServletRequest req) {
        return getAsyncRequest(req.isAsyncStarted() ? req.getAsyncContext() : req.startAsync());
    }

    /**
     * Translates the provide {@link AsyncContext} to an instance of {@link HttpRequest}.  The provided
     * {@link AsyncContext} must have been started from an {@link HttpServletRequest} for this to function properly.
     *
     * @param asyncContext an {@link AsyncContext} created from an instance of {@link HttpServletRequest}.
     *
     * @return the {@link Request} instance translated from the provided {@link HttpServletRequest}
     */
    HttpRequest getAsyncRequest(final AsyncContext asyncContext);

}
