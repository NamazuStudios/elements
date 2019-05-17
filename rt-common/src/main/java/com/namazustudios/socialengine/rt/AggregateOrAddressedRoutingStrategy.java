package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

public class AggregateOrAddressedRoutingStrategy implements RoutingStrategy {
    private RemoteAddressRegistry remoteAddressRegistry;

    public Set<String> getDestinationAddresses(RoutingAddressProvider routingAddressProvider) {
        final Set<String> routingAddresses;

        if (routingAddressProvider.getRoutingAddress() != null) {
            routingAddresses = Collections.singleton(routingAddressProvider.getRoutingAddress());
        }
        else {
            routingAddresses = getRemoteAddressRegistry().getAllRemoteAddresses();
        }

        return routingAddresses;
    }

    public RemoteAddressRegistry getRemoteAddressRegistry() {
        return remoteAddressRegistry;
    }

    @Inject
    public void setRemoteAddressRegistry(RemoteAddressRegistry remoteAddressRegistry) {
        this.remoteAddressRegistry = remoteAddressRegistry;
    }
}
