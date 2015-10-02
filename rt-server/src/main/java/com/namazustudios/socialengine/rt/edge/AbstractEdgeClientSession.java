package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.Observation;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.internal.InternalServer;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 10/2/15.
 */
public abstract class AbstractEdgeClientSession implements EdgeClientSession {

    @Inject
    private EdgeServer edgeServer;

    @Inject
    private InternalServer internalServer;

    @Override
    public EventObservationTypeBuilder<Observation> observeEdgeEvent(String name) {
        return null;
    }

    @Override
    public EventObservationTypeBuilder<Observation> observeInternalEvent(String name) {
        return null;
    }

    @Override
    public EventObservationTypeBuilder<Subscription> subscribeToInternalEvent(String name) {
        return null;
    }

    @Override
    public EventObservationTypeBuilder<Subscription> subscribeToEdgeEvent(String name) {
        return null;
    }

    @Override
    public void addDisconnectListener(EdgeClientSessionListener edgeClientSessionListener) {

    }

    @Override
    public void addIdleListener(EdgeClientSessionListener edgeClientSessionListener) {

    }


}
