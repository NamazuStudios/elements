package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.*;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by patricktwohig on 10/2/15.
 */
public abstract class AbstractEdgeClientSession implements EdgeClientSession {

    private static final String IDLE_OBSERVERS_KEY = AbstractEdgeClientSession.class + "IDLE_OBSERVERS_KEY";

    private static final String DISCONNECT_OBSERVERS_KEY = AbstractEdgeClientSession.class + "IDLE_OBSERVERS_KEY";

    private EventService eventService;

    private Observation createObservation(final String named, final Class<?> type, final Path path) {

        final Observation serverObservation = getEventService().observe(path, named, getEventReceiver(type));

        final Observation disconnectObservation = observeDisconnect(() -> {
            serverObservation.release();
            return false;
        });

        return () -> {
            serverObservation.release();
            disconnectObservation.release();
        };

    }

    @Override
    public Observation observeIdle(EdgeClientSessionObserver edgeClientSessionObserver) {
        return addObserver(IDLE_OBSERVERS_KEY, edgeClientSessionObserver);
    }

    @Override
    public Observation observeDisconnect(EdgeClientSessionObserver edgeClientSessionObserver) {
        return addObserver(DISCONNECT_OBSERVERS_KEY, edgeClientSessionObserver);
    }

    private Observation addObserver(final String key, EdgeClientSessionObserver edgeClientSessionObserver) {

        final UUID uuid = UUID.randomUUID();
        final ConcurrentNavigableMap<UUID, EdgeClientSessionObserver> observers = getObservers(key);

        observers.put(uuid, edgeClientSessionObserver);

        return () -> observers.remove(uuid);

    }

    private ConcurrentNavigableMap<UUID, EdgeClientSessionObserver> getObservers(final String key) {

        ConcurrentNavigableMap<UUID, EdgeClientSessionObserver> observers;
        observers = getSessionVariableTyped(key, ConcurrentNavigableMap.class);

        if (observers == null) {

            final ConcurrentNavigableMap<UUID, EdgeClientSessionObserver> tmp = new ConcurrentSkipListMap<>();
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

        final ConcurrentNavigableMap<UUID, EdgeClientSessionObserver> observers = getObservers(key);
        final Iterator<Map.Entry<UUID, EdgeClientSessionObserver>> entryIterator = observers.entrySet().iterator();

        while (entryIterator.hasNext()) {

            final Map.Entry<UUID, EdgeClientSessionObserver> entry = entryIterator.next();

            if (!entry.getValue().observe()) {
                entryIterator.remove();
            }

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
