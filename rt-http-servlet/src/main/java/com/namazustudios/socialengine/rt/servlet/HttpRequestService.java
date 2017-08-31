package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This translates the {@link HttpServletRequest} instances to {@link Request} which can be passed to the
 * underlying handlers.
 */
public interface HttpRequestService {

    /**
     * Translates the provide {@link HttpServletRequest} to an instance of {@link Request}.  The
     * {@link HttpServletResponse} is also provided for context if it is needed.  However
     * it is not recommended that the implementation modify the {@link HttpServletResponse}.
     *
     * @param req the {@link HttpServletRequest}
     * @param rsp the {@link HttpServletResponse}
     *
     * @return the {@link Request} instance translated from the provided {@link HttpServletRequest}
     */
    HttpRequest getRequest(final HttpServletRequest req, final HttpServletResponse rsp);

}
