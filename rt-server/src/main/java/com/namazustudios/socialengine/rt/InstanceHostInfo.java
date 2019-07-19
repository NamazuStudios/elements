package com.namazustudios.socialengine.rt;

import java.util.Objects;

/**
 * Gets host information for an instance.  Objects implementing this interface must implement {@link Object#hashCode()}
 * and {@link Object#equals(Object)} such that this can be used as a hash map key.
 */
public interface InstanceHostInfo {

    /**
     * Gets the address for the invoker service
     *
     * @return the invoker address
     */
    String getConnectAddress();

}
