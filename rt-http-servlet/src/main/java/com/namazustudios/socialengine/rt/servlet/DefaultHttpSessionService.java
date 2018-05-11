package com.namazustudios.socialengine.rt.servlet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.namazustudios.socialengine.rt.DummySession;
import com.namazustudios.socialengine.rt.Observation;
import com.namazustudios.socialengine.rt.handler.HandlerClientSessionObserver;
import com.namazustudios.socialengine.rt.handler.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
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
 * Not currently supported.  Implemented using {@link DummySession}.
 */
@Singleton
@WebListener
public class DefaultHttpSessionService implements HttpSessionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpSessionService.class);

    private final Multimap<String, HandlerClientSessionObserver> sessionDestroyedObservers = HashMultimap.create();

    private final ExecutorService sessionListenerScheduler = newSingleThreadScheduledExecutor(r -> {
        logger.info("Creating thread: {}", r);
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(DefaultHttpSessionService.class.getName() + " worker thread.");
        thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
        return thread;
    });

    @Override
    public Session getSession(final HttpServletRequest req) {
        return new DummySession();
    }

}
