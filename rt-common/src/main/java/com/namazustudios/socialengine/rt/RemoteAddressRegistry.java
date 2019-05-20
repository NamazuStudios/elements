package com.namazustudios.socialengine.rt;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class RemoteAddressRegistry {
    private final AtomicReference<Set<String>> atomicRemoteAddresses = new AtomicReference<>(new HashSet<>());

    public void registerRemoteAddress(String address) {
        final Set<String> remoteAddresses = atomicRemoteAddresses.get();
        remoteAddresses.add(address);
    }

    public boolean unregisterRemoteAddress(String address) {
        final Set<String> remoteAddresses = atomicRemoteAddresses.get();
        if (remoteAddresses.contains(address)) {
            remoteAddresses.remove(address);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean containsRemoteAddress(String address) {
        final Set<String> remoteAddresses = atomicRemoteAddresses.get();
        return remoteAddresses.contains(address);
    }

    Set<String> getAllRemoteAddresses() {
        final Set<String> remoteAddresses = atomicRemoteAddresses.get();
        return remoteAddresses.stream().collect(Collectors.toSet());
    }
}
