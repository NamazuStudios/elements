package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class JeroMQRemoteInvokerRegistry implements RemoteInvokerRegistry {
    private final AtomicReference<Map<String, RemoteInvoker>> atomicRemoteInvokerMapReference = new AtomicReference<>(new HashMap<>());

    @Override
    public void registerRemoteInvoker(String address, RemoteInvoker remoteInvoker) {
        final Map<String, RemoteInvoker> remoteInvokerMap = atomicRemoteInvokerMapReference.get();
        remoteInvokerMap.put(address, remoteInvoker);
    }

    @Override
    public boolean unregisterRemoteInvoker(String address) {
        final Map<String, RemoteInvoker> remoteInvokerMap = atomicRemoteInvokerMapReference.get();
        if (remoteInvokerMap.containsKey(address)) {
            remoteInvokerMap.remove(address);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public RemoteInvoker getRemoteInvoker(String address) {
        final Map<String, RemoteInvoker> remoteInvokerMap = atomicRemoteInvokerMapReference.get();
        return remoteInvokerMap.get(address);
    }

    @Override
    public Set<RemoteInvoker> getAllRemoteInvokers() {
        final Map<String, RemoteInvoker> remoteInvokerMap = atomicRemoteInvokerMapReference.get();
        final Set<RemoteInvoker> remoteInvokers = remoteInvokerMap.values().stream().collect(Collectors.toSet());
        return remoteInvokers;
    }
}