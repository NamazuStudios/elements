//package com.namazustudios.socialengine.rt;
//
//import com.namazustudios.socialengine.rt.RoutingAddressProvider;
//import com.namazustudios.socialengine.rt.remote.RoutingStrategy;
//
//import javax.inject.Inject;
//import java.util.Set;
//
///**
// * The invocation should be called on all application nodes in the network. Note that this is an
// * "all-or-nothing" call for now, i.e. if any single application node invocation fails, then this entire
// * procedure will fail.
// *
// * If the invocation originated on an app node, that same app node will also receive the invocation but without
// * entering the network layer.
// */
//public class AggregateRoutingStrategy implements RoutingStrategy {
//    private RemoteAddressRegistry remoteAddressRegistry;
//
//    public Set<String> getDestinationAddresses(RoutingAddressProvider routingAddressProvider) {
//        final Set<String> routingAddresses = getRemoteAddressRegistry().getAllRemoteAddresses();
//
//        return routingAddresses;
//    }
//
//    public RemoteAddressRegistry getRemoteAddressRegistry() {
//        return remoteAddressRegistry;
//    }
//
//    @Inject
//    public void setRemoteAddressRegistry(RemoteAddressRegistry remoteAddressRegistry) {
//        this.remoteAddressRegistry = remoteAddressRegistry;
//    }
//}
