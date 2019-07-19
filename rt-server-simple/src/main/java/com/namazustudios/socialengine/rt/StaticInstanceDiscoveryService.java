package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;

public class StaticInstanceDiscoveryService implements InstanceDiscoveryService {

    public static final String HOST_INFO = "com.namazustudios.socialengine.rt.static.host.info";

    private Set<InstanceHostInfo> hostInfoSet;

    private final AtomicBoolean running = new AtomicBoolean();

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) throw new IllegalStateException("Already running.");
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) throw new IllegalStateException("Not running.");
    }

    @Override
    public Set<InstanceHostInfo> getKnownHosts() {
        if (!running.get())  throw new IllegalStateException("Not running.");
        return unmodifiableSet(getHostInfoSet());
    }

    @Override
    public Subscription subscribeToDiscovery(final Consumer<InstanceHostInfo> instanceHostInfoConsumer) {
        return () -> {};
    }

    @Override
    public Subscription subscribeToUndiscovery(final Consumer<InstanceHostInfo> instanceHostInfoConsumer) {
        return () -> {};
    }

    public Set<InstanceHostInfo> getHostInfoSet() {
        return hostInfoSet;
    }

    @Inject
    public void setHostInfoSet(@Named(HOST_INFO) Set<InstanceHostInfo> hostInfoSet) {
        this.hostInfoSet = hostInfoSet;
    }

}
