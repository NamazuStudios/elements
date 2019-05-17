package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.rt.remote.RemoteAddressRegistry;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.srv.SrvUniqueIdentifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class JeroMQRemoteAddressRegistry implements RemoteAddressRegistry {
    private final AtomicReference<Map<SrvUniqueIdentifier, String>> atomicRemoteAddressMapReference = new AtomicReference<>(new HashMap<>());

    @Override
    public void registerRemoteAddress(SrvUniqueIdentifier srvUniqueIdentifier, String address) {
        final Map<SrvUniqueIdentifier, String> remoteAddressMap = atomicRemoteAddressMapReference.get();
        remoteAddressMap.put(srvUniqueIdentifier, address);
    }

    @Override
    public boolean unregisterRemoteAddress(SrvUniqueIdentifier srvUniqueIdentifier) {
        final Map<SrvUniqueIdentifier, String> remoteAddressMap = atomicRemoteAddressMapReference.get();
        if (remoteAddressMap.containsKey(srvUniqueIdentifier)) {
            remoteAddressMap.remove(srvUniqueIdentifier);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public String getRemoteAddress(SrvUniqueIdentifier srvUniqueIdentifier) {
        final Map<SrvUniqueIdentifier, String> remoteAddressMap = atomicRemoteAddressMapReference.get();
        return remoteAddressMap.get(srvUniqueIdentifier);
    }

    @Override
    public Set<String> getAllRemoteAddresses() {
        final Map<SrvUniqueIdentifier, String> remoteAddressMap = atomicRemoteAddressMapReference.get();
        final Set<String> remoteAddresses = remoteAddressMap.values().stream().collect(Collectors.toSet());
        return remoteAddresses;
    }
}