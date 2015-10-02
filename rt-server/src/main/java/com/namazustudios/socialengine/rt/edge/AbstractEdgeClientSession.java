package com.namazustudios.socialengine.rt.edge;

/**
 * Created by patricktwohig on 10/2/15.
 */
public abstract class AbstractEdgeClientSession implements EdgeClientSession {

    @Override
    public EventSubscriptionTypeBuilder subscribeToInternalEvent(String name) {
        return null;
    }

    @Override
    public EventSubscriptionTypeBuilder subscribeToEdgeEvent(String name) {
        return null;
    }

    @Override
    public void addDisconnectListener(EdgeClientSessionListener edgeClientSessionListener) {

    }

    @Override
    public void addIdleListener(EdgeClientSessionListener edgeClientSessionListener) {

    }

}
