package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.*;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by patricktwohig on 10/2/15.
 */
public abstract class AbstractSession implements Session {

    private static final String IDLE_OBSERVERS_KEY = AbstractSession.class + "IDLE_OBSERVERS_KEY";

    private static final String DISCONNECT_OBSERVERS_KEY = AbstractSession.class + "IDLE_OBSERVERS_KEY";

    private EventService eventService;

    private Observation createObservation(final String named, final Class<?> type, final Path path) {

        final Observation serverObservation = getEventService().observe(path, named, getEventReceiver(type));

        final Observation disconnectObservation = observeDisconnect(() -> {
            serverObservation.release();
        });

        return () -> {
            serverObservation.release();
            disconnectObservation.release();
        };

    }

    @Override
    public Observation observeIdle(HandlerClientSessionObserver handlerClientSessionObserver) {
        return addObserver(IDLE_OBSERVERS_KEY, handlerClientSessionObserver);
    }

    @Override
    public Observation observeDisconnect(HandlerClientSessionObserver handlerClientSessionObserver) {
        return addObserver(DISCONNECT_OBSERVERS_KEY, handlerClientSessionObserver);
    }

    private Observation addObserver(final String key, HandlerClientSessionObserver handlerClientSessionObserver) {

        final UUID uuid = UUID.randomUUID();
        final ConcurrentNavigableMap<UUID, HandlerClientSessionObserver> observers = getObservers(key);

        observers.put(uuid, handlerClientSessionObserver);

        return () -> observers.remove(uuid);

    }

    private ConcurrentNavigableMap<UUID, HandlerClientSessionObserver> getObservers(final String key) {

        ConcurrentNavigableMap<UUID, HandlerClientSessionObserver> observers;
        observers = getSessionVariableTyped(key, ConcurrentNavigableMap.class);

        if (observers == null) {

            final ConcurrentNavigableMap<UUID, HandlerClientSessionObserver> tmp = new ConcurrentSkipListMap<>();
            observers = (ConcurrentSkipListMap) setSessionVariableIfAbsent(key, tmp);

            if (observers == null) {
                observers = tmp;
            }

        }

        return observers;

    }

    /**
     * Returns an instance of {@link EventReceiver} which can be used to receive instances of
     * {@link Event}.  The returned receiver will relay the events to the client on the
     * other end of this connection
     *
     * @return an instance of {@link EventReceiver}
     */
    public abstract <T> EventReceiver<T> getEventReceiver(final Class<T> type);

    /**
     * Dispatches the "Idle" event.
     */
    public void dispatchIdle() {
        dispatch(IDLE_OBSERVERS_KEY);
    }

    /**
     * Dispatches the "Disconnect" event.
     */
    public void dispatchDisconnect() {
        dispatch(DISCONNECT_OBSERVERS_KEY);
    }

    private void dispatch(final String key) {

        final ConcurrentNavigableMap<UUID, HandlerClientSessionObserver> observers = getObservers(key);
        final Iterator<Map.Entry<UUID, HandlerClientSessionObserver>> entryIterator = observers.entrySet().iterator();

        while (entryIterator.hasNext()) {
            final Map.Entry<UUID, HandlerClientSessionObserver> entry = entryIterator.next();
            entryIterator.remove();
        }

    }

    public EventService getEventService() {
        return eventService;
    }

    @Inject
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

}
