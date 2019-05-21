package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;

import java.util.Set;

public interface RemoteInvokerRegistry {

// I dont' think these need to be part of the public interface.  The registry shoudl be aware of
// the changes to the network and react accordingly on its own.
//    void registerRemoteInvoker(String address, RemoteInvoker remoteInvoker);
//    boolean unregisterRemoteInvoker(String address);

    RemoteInvoker getAnyRemoteInvoker();

    /**
     * Returns all registered {@link RemoteInvoker}s.
     * @return a {@link Set} of {@link RemoteInvoker}s.
     */
    Set<RemoteInvoker> getAllRemoteInvokers();

    /**
     * Returns the {@link RemoteInvoker} registered under the given address identifier.
     *
     * @param address the node identifier string.
     * @return a RemoteInvoker for the given address.
     */
    RemoteInvoker getRemoteInvoker(Object address);

}
