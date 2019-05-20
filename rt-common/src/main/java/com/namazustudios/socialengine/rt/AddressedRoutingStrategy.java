//package com.namazustudios.socialengine.rt.remote;
//
//import com.namazustudios.socialengine.rt.RoutingAddressProvider;
//
//import java.util.Collections;
//import java.util.Set;
//
///**
// * The invocation is addressed to a specific resource on a specific application node as defined within a
// * ResourceId.
// *
// * If the invocation originated on an app node and matches the local app node UUID, then the invocation will
// * occur without entering the network layer.
// */
//public class AddressedRoutingStrategy implements RoutingStrategy {
//    public Set<String> getDestinationAddresses(RoutingAddressProvider routingAddressProvider) {
//        final String routingAddress = routingAddressProvider.getRoutingAddress();
//
//        return Collections.singleton(routingAddress);
//    }
//}
