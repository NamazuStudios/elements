package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.Path;

/**
 * Created by patricktwohig on 10/5/15.
 */
public abstract class AbstractObservationNameBuilder<ObservationT>
        implements EventObservationNameBuilder<ObservationT> {

    @Override
    public EventObservationTypeBuilder<ObservationT> named(final String name) {
        return new EventObservationTypeBuilder<ObservationT>() {
            @Override
            public EventObservationPathBuilder<ObservationT> ofAnyType() {
                return ofType(Object.class);
            }

            @Override
            public EventObservationPathBuilder<ObservationT> ofType(final String type) {
                try {
                    return ofType(Class.forName(type));
                } catch (ClassNotFoundException e) {
                    throw new InternalException(e);
                }
            }

            @Override
            public <T> EventObservationPathBuilder<ObservationT> ofType(final Class<T> type) {
                return eventObservationPathBuilder(name, type);
            }
        };
    }

    private EventObservationPathBuilder<ObservationT> eventObservationPathBuilder(final String named,
                                                                                  final Class<?> type) {
        return new EventObservationPathBuilder<ObservationT>() {
            @Override
            public ObservationT atPath(String path) {
                return atPath(new Path(path));
            }

            @Override
            public ObservationT atPath(Path path) {
                return doCreateObservation(named, type, path);
            }
        };
    }

    protected abstract ObservationT doCreateObservation(final String named, final Class<?> type, final Path path);

}
