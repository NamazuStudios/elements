package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.RoutingAddressProvider;
import com.namazustudios.socialengine.rt.RoutingStrategy;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

/**
 * The invocation should occur on the least-resource-constrained app node (this may be defined as the app node
 * that, at the time of the local invocation, had reported the smallest load avg or the smallest in-memory
 * resources), e.g. a single use invocation. Necessarily, an ANY invocation is neither addressed, nor should it
 * be run more than exactly once on exactly one app node in the network.
 *
 * If the invocation originated on an app node, that same app node will be considered as a viable candidate for
 * performing the invocation. The resource utilization poll of the local app node should not occur over the
 * network and neither should the local invocation (should the local app node be chosen to perform it).
 */
public class AnyRoutingStrategy implements RoutingStrategy {
    private ResourceAvailabilityMonitorService resourceAvailabilityMonitorService;

    public Set<String> getDestinationAddresses(RoutingAddressProvider routingAddressProvider) {
        final String routingAddress = getResourceAvailabilityMonitorService().getOptimalLoadAverageAddress();

        return Collections.singleton(routingAddress);
    }

    public ResourceAvailabilityMonitorService getResourceAvailabilityMonitorService() {
        return resourceAvailabilityMonitorService;
    }

    @Inject
    public void setResourceAvailabilityMonitorService(ResourceAvailabilityMonitorService resourceAvailabilityMonitorService) {
        this.resourceAvailabilityMonitorService = resourceAvailabilityMonitorService;
    }
}
