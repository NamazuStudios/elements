package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.internal.InternalResource;
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
    public PathBuilder<EventObservationNameBuilder<Observation>> observeEdgeEvent() {
        return new AbstractEventPathBuilder<Observation>() {
            @Override
            protected Observation doCreateObservation(final Path path, final String named,
                                                      final Class<?> type) {
                return createObservation(edgeServer, named, type, path);
            }
        };
    }

    @Override
    public PathBuilder<EventObservationNameBuilder<Observation>> observeInternalEvent() {
        return new AbstractEventPathBuilder<Observation>() {
            @Override
            protected Observation doCreateObservation(final Path path, final String named,
                                                      final Class<?> type) {
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
    public PathBuilder<InternalResource> retainInternalResource() {
        return new AbstractPathBuilder<InternalResource>() {
            @Override
            public InternalResource atPath(final Path path) {

                final InternalResource internalResource = internalServer.retain(path);

                observeDisconnect(new EdgeClientSessionObserver() {
                    @Override
                    public boolean observe() {
                        internalResource.release();
                        return false;
                    }
                });

                return internalResource;

            }
        };
    }

    @Override
    public PathBuilder<InternalResource> retainOrAddResourceIfAbsent(final ResourceInitializer<InternalResource> resourceInitializer) {
        return new AbstractPathBuilder<InternalResource>() {
            @Override
            public InternalResource atPath(Path path) {

                final InternalResource internalResource = internalServer.retainOrAddResourceIfAbsent(path, resourceInitializer);

                observeDisconnect(new EdgeClientSessionObserver() {
                    @Override
                    public boolean observe() {
                        internalResource.release();
                        return false;
                    }
                });

                return internalResource;

            }
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

        return new Observation() {
            @Override
            public void release() {
                observers.remove(uuid);
            }
        };

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

    /**
     * Created by patricktwohig on 10/5/15.
     */
    private abstract static class AbstractEventPathBuilder<ObservationT>
            implements PathBuilder<EventObservationNameBuilder<ObservationT>> {

        @Override
        public EventObservationNameBuilder<ObservationT> atPath(final String path) {
            return atPath(new Path(path));
        }

        @Override
        public EventObservationNameBuilder<ObservationT> atPath(final Path path) {
            return null;
        }

        private EventObservationNameBuilder<ObservationT> eventObservationNameBuilder(final Path path) {
            return new EventObservationNameBuilder<ObservationT>() {
                @Override
                public EventObservationTypeBuilder<ObservationT> named(String name) {
                    return null;
                }
            };
        }

        private EventObservationTypeBuilder<ObservationT> eventObservationTypeBuilder(final Path path,
                                                                                      final String named) {
            return new EventObservationTypeBuilder<ObservationT>() {
                @Override
                public ObservationT ofAnyType() {
                    return ofType(Object.class);
                }

                @Override
                public ObservationT ofType(String type) {
                    try {
                        return ofType(Class.forName(type));
                    } catch (ClassNotFoundException e) {
                        throw new InternalException(e);
                    }
                }

                @Override
                public <T> ObservationT ofType(Class<T> type) {
                    return doCreateObservation(path, named, type);
                }
            };
        }

        protected abstract ObservationT doCreateObservation(final Path path, final String named, final Class<?> type);

    }

}
