package com.namazustudios.socialengine.rt.servlet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.namazustudios.socialengine.rt.Observation;
import com.namazustudios.socialengine.rt.handler.HandlerClientSessionObserver;
import com.namazustudios.socialengine.rt.handler.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Creates instances of {@link ServletSession} which are backed by the underlying {@Link HttpSession}.  This emulates
 * the disconnection event, as observed by {@link Session#observeDisconnect(HandlerClientSessionObserver)} as well as
 * {@link Session#observeIdle(HandlerClientSessionObserver)} by following the callbacks supplied to
 * {@link HttpSessionListener}.
 */
@WebListener
public class ServletSessionService implements HttpSessionService, HttpSessionListener {

    private static final Logger logger = LoggerFactory.getLogger(ServletSessionService.class);

    private final Multimap<String, HandlerClientSessionObserver> sessionDestroyedObservers = HashMultimap.create();

    private final ExecutorService sessionListenerScheduler = newSingleThreadScheduledExecutor(r -> {
        logger.info("Creating thread: {}", r);
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(ServletSessionService.class.getName() + " worker thread.");
        return thread;
    });

    @Override
    public Session getSession(final HttpServletRequest req) {
        return new ServletSession(
            req::getSession,
            h -> observeIdle(req::getSession, h),
            h -> observeDisconnect(req::getSession, h));
    }

    @Override
    public void sessionCreated(final HttpSessionEvent se) {
        logger.info("Created HTTP session {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent se) {

        logger.info("Destroyed HTTP session {}", se.getSession().getId());

        final String sid = se.getSession().getId();

        sessionListenerScheduler.submit(() -> {

            // Removes all sessions and sends messages to the various configured observer instances
            for (final HandlerClientSessionObserver observer : sessionDestroyedObservers.removeAll(sid)) {
                observer.observe();
            }

        });

    }

    private Observation observeIdle(final Supplier<HttpSession> httpSessionSupplier,
                                    final HandlerClientSessionObserver handlerClientSessionObserver) {
        logger.warn("Observing idle session is not supported for Servlet HTTP Sessions.");
        return () -> {};
    }

    private Observation observeDisconnect(final Supplier<HttpSession> httpSessionSupplier,
                                          final HandlerClientSessionObserver handlerClientSessionObserver) {

        final String sid = httpSessionSupplier.get().getId();

        logger.info("Observing disconnect of session {}.", sid);

        final AtomicBoolean alive = new AtomicBoolean();

        final HandlerClientSessionObserver wrapped = () -> {
            if (alive.get()) handlerClientSessionObserver.observe();
        };

        sessionListenerScheduler.submit(() -> sessionDestroyedObservers.put(sid, wrapped));

        return () -> {
            alive.set(false);
            sessionListenerScheduler.submit(() -> {
                sessionDestroyedObservers.remove(sid, wrapped);
            });
        };

    }

}
