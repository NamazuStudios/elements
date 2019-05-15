package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;

import java.util.Set;

public interface RemoteInvokerRegistry {
    void registerRemoteInvoker(String address, RemoteInvoker remoteInvoker);
    boolean unregisterRemoteInvoker(String address);

    /**
     * Returns the {@link RemoteInvoker} registered under the given address identifier.
     *
     * @param address the node identifier string.
     * @return a RemoteInvoker for the given address.
     */
    RemoteInvoker getRemoteInvoker(String address) throws NodeNotFoundException;

    /**
     * Returns all registered {@link RemoteInvoker}s.
     * @return a {@link Set} of {@link RemoteInvoker}s.
     */
    Set<RemoteInvoker> getAllRemoteInvokers();
}