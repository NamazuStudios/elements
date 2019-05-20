package com.namazustudios.socialengine.rt;

import java.util.Collections;
import java.util.Set;

public interface RoutingStrategy {

    Set<String> getDestinationAddresses(RoutingAddressProvider routingAddressProvider);

    class DefaultRoutingStrategy implements RoutingStrategy {

        // TODO PLaceholder for now

        @Override
        public Set<String> getDestinationAddresses(RoutingAddressProvider routingAddressProvider) {
            return Collections.emptySet();
        }
    }

}
