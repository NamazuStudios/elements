//package com.namazustudios.socialengine.rt;
//
//import com.namazustudios.socialengine.rt.RoutingAddressProvider;
//import com.namazustudios.socialengine.rt.remote.RoutingStrategy;
//
//import javax.inject.Inject;
//import java.util.Collections;
//import java.util.Set;
//
//public class AnyOrAddressedRoutingStrategy implements RoutingStrategy {
//    private InstanceResourceMonitorService resourceAvailabilityMonitorService;
//
//    public Set<String> getDestinationAddresses(RoutingAddressProvider routingAddressProvider) {
//        final String routingAddress;
//
//        if (routingAddressProvider.getRoutingAddress() != null) {
//            routingAddress = routingAddressProvider.getRoutingAddress();
//        }
//        else {
//            routingAddress = getResourceAvailabilityMonitorService().getInstanceUuidByOptimalLoadAverage();
//        }
//
//        return Collections.singleton(routingAddress);
//    }
//
//    public InstanceResourceMonitorService getResourceAvailabilityMonitorService() {
//        return resourceAvailabilityMonitorService;
//    }
//
//    @Inject
//    public void setResourceAvailabilityMonitorService(InstanceResourceMonitorService resourceAvailabilityMonitorService) {
//        this.resourceAvailabilityMonitorService = resourceAvailabilityMonitorService;
//    }
//}
