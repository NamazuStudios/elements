package dev.getelements.elements.rt.servlet;

import dev.getelements.elements.rt.handler.Session;

import javax.servlet.http.HttpServletRequest;

/**
 * Translates instances of {@link javax.servlet.http.HttpServletRequest} into {@link Session} which
 * can be passed to the underlying handlers.
 */
public interface HttpSessionService {

    /**
     * Translates the provide {@link HttpServletRequest} to an instance of {@link Session}.  The underlying
     * {@link Session} should avoid actually creating the session until it is needed.
     *
     * @param req the {@link HttpServletRequest} instance
     *
     * @return the {@link Session} instance translated from the provided {@link HttpServletRequest}
     */
    Session getSession(final HttpServletRequest req);

}
