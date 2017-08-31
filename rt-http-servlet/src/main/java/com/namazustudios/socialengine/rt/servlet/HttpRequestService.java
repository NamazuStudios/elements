package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * This translates the {@link HttpServletRequest} instances to {@link Request} which can be passed to the
 * underlying handlers.
 */
public interface HttpRequestService {

    /**
     * Translates the provide {@link HttpServletRequest} to an instance of {@link HttpRequest}.
     *
     * @param req the {@link HttpServletRequest}
     *
     * @return the {@link Request} instance translated from the provided {@link HttpServletRequest}
     */
    HttpRequest getRequest(final HttpServletRequest req);

}
