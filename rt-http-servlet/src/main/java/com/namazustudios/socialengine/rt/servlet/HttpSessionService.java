package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.handler.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Translates instances of {@link javax.servlet.http.HttpServletRequest} into {@link Session} which
 * can be passed to the underlying handlers.
 */
public interface HttpSessionService {

    /**
     * Translates the provide {@link HttpServletRequest} to an instance of {@link Session}.  The
     * {@link HttpServletResponse} is also provided for context if it is needed.  However
     * it is not recommended that the implementation modify the {@link HttpServletResponse}.
     *
     * @param req the {@link HttpServletRequest} instance
     * @param rsp the {@link HttpServletResponse} instance
     *
     * @return the {@link Session} instance translated from the provided {@link HttpServletRequest}
     */
    Session getRequest(final HttpServletRequest req, final HttpServletResponse rsp);

}
