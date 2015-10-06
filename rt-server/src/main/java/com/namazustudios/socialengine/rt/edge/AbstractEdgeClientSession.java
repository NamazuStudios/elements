package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.internal.InternalServer;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by patricktwohig on 10/2/15.
 */
public abstract class AbstractEdgeClientSession implements EdgeClientSession {

    private static final String IDLE_OBSERVERS_KEY = AbstractEdgeClientSession.class + "IDLE_OBSERVERS_KEY";

    private static final String DISCONNECT_OBSERVERS_KEY = AbstractEdgeClientSession.class + "IDLE_OBSERVERS_KEY";

    @Inject
    private EdgeServer edgeServer;

    @Inject
    private InternalServer internalServer;

    @Override
    public EventObservationNameBuilder<Observation> observeEdgeEvent() {
        return new AbstractObservationNameBuilder<Observation>() {
            @Override
            protected Observation doCreateObservation(final String named,
                                                      final Class<?> type,
                                                      final Path path) {
                return createObservation(edgeServer, named, type, path);
            }
        };
    }

    @Override
    public EventObservationNameBuilder<Observation> observeInternalEvent() {
        return new AbstractObservationNameBuilder<Observation>() {
            @Override
            protected Observation doCreateObservation(final String named,
                                                      final Class<?> type,
                                                      final Path path) {
                return createObservation(internalServer, named, type, path);
            }
        };

    }

    private Observation createObservation(final Server server, final String named,
                                          final Class<?> type, final Path path) {

        final Observation serverObservation = server.observe(path, named, getEventReceiver(type));
        final Observation disconnectObservation = observeDisconnect(new EdgeClientSessionObserver() {
            @Override
            public boolean observe() {
                serverObservation.release();
                return false;
            }
        });

        return new Observation() {
            @Override
            public void release() {
                serverObservation.release();
                disconnectObservation.release();
            }
        };

    }

    @Override
    public EventObservationNameBuilder<List<Subscription>> subscribeToEdgeEvent() {
        return new AbstractObservationNameBuilder<List<Subscription>>() {

            @Override
            protected List<Subscription> doCreateObservation(final String named,
                                                             final Class<?> type,
                                                             final Path path) {
                return createSubscriptions(edgeServer, named, type, path);
            }
        };
    }

    @Override
    public EventObservationNameBuilder<List<Subscription>> subscribeToInternalEvent() {
        return new AbstractObservationNameBuilder<List<Subscription>>() {

            @Override
            protected List<Subscription> doCreateObservation(final String named,
                                                             final Class<?> type,
                                                             final Path path) {
                return createSubscriptions(internalServer, named, type, path);
            }
        };
    }


    private <T> List<Subscription> createSubscriptions(final Server server, final String named,
                                                       final Class<T> type, final Path path) {

        final List<Subscription> subscriptions = server.subscribe(path, named, getEventReceiver(type));
        final AtomicInteger count = new AtomicInteger(subscriptions.size());

        final Observation disconnectObservation = observeDisconnect(new EdgeClientSessionObserver() {
            @Override
            public boolean observe() {

                for (final Subscription subscription : subscriptions) {
                    subscription.release();
                }

                return false;
            }

        });

        final List<Subscription> out = new ArrayList<>();

        for (final Subscription underlyingSubscription : subscriptions) {
            out.add(new Subscription() {
                @Override
                public Path getPath() {
                    return underlyingSubscription.getPath();
                }

                @Override
                public void release() {

                    underlyingSubscription.release();

                    if (count.decrementAndGet() <= 0) {
                        disconnectObservation.release();
                    }

                }
            });
        }

        return out;
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

        return new Observation() {
            @Override
            public void release() {
                observers.remove(uuid);
            }
        };

    }

    private ConcurrentNavigableMap<UUID, EdgeClientSessionObserver> getObservers(final String key) {

        ConcurrentNavigableMap<UUID, EdgeClientSessionObserver> observers;
        observers = getSessionVariable(key, ConcurrentNavigableMap.class);

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

}
