package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Subscription;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableSet;

public class StaticInstanceDiscoveryService implements InstanceDiscoveryService {

    public static final String STATIC_HOST_INFO = "com.namazustudios.socialengine.rt.static.host.info";

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
    public void setHostInfoSet(@Named(STATIC_HOST_INFO) Set<InstanceHostInfo> hostInfoSet) {
        this.hostInfoSet = hostInfoSet;
    }

}
