package com.namazustudios.socialengine.rt;

import java.util.Set;

public interface RoutingStrategy {
    Set<String> getDestinationAddresses(RoutingAddressProvider routingAddressProvider);
}
