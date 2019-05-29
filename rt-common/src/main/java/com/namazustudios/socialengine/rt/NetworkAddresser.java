package com.namazustudios.socialengine.rt;

/**
 * Signifies that an object adhering to this interface provides information to address Remote invocations to a
 * particular Instance in the deployment.
 */
public interface NetworkAddresser {
    Object getNetworkAddressAlias();
}
