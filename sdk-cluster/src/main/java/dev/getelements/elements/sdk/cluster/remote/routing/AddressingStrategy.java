package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.address.ElementMethodAddress;
import dev.getelements.elements.sdk.cluster.address.RemoteElementMethodAddress;
import dev.getelements.elements.sdk.cluster.remote.annotation.RoutingAddressSource;

/**
 * A type which can write the {@link ElementMethodAddress} just in time for dispatch allowing routing to go to a
 * specific node. This combines the data from the input address and arguments annotated with
 * {@link RoutingAddressSource}.
 */
@FunctionalInterface
public interface AddressingStrategy {

    /**
     * The method address.
     *
     * @param input the input
     * @return the address
     */
    RemoteElementMethodAddress resolve(RemoteElementMethodAddress input, Object[] arguments);

}
