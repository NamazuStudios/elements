package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.cluster.address.RemoteElementMethodAddress;

/**
 * Performs no modification to the {@link RemoteElementMethodAddress} and passes it straight through as-is.
 */
public class DefaultAddressingStrategy implements AddressingStrategy {

    @Override
    public RemoteElementMethodAddress resolve(final RemoteElementMethodAddress input, final Object[] args) {
        return input;
    }

}
