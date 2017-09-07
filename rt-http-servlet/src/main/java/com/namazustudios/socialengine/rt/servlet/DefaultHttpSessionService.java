package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.handler.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class DefaultHttpSessionService implements HttpSessionService, HttpSessionListener {

    @Override
    public Session getSession(final HttpServletRequest req) {
        return null;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

    }

}
