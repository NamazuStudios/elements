package com.namazustudios.socialengine.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.InstanceResourceMonitorService;
import com.namazustudios.socialengine.rt.NodeMetadataContext;
import com.namazustudios.socialengine.rt.SrvUniqueIdentifier;
import com.namazustudios.socialengine.rt.exception.HandlerTimeoutException;
import com.namazustudios.socialengine.rt.remote.ProxyBuilder;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class JeroMQInstanceResourceMonitorService implements InstanceResourceMonitorService {
    private final AtomicBoolean atomicIsRunning = new AtomicBoolean(false);

    private final AtomicReference<Map<UUID, InstanceMetadataContext>> atomicInstanceMetadataContexts = new AtomicReference<>(new HashMap<>());

    // TODO: load expected size from properties
    private final AtomicReference<BiMap<UUID, Double>> atomicMostRecentLoadAverages = new AtomicReference<>(HashBiMap.create(16));

    private Provider<RemoteInvoker> remoteInvokerProvider;
    private Provider<ProxyBuilder<InstanceMetadataContext>> instanceMetadataContextProxyBuilderProvider;

    private void onInstanceConnected(final SrvUniqueIdentifier srvUniqueIdentifier, final UUID instanceUuid) {
        final String connectAddress = "tcp://" + srvUniqueIdentifier.getHost() + ":" + srvUniqueIdentifier.getPort();
        final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();
        // TODO: get the timeout millis from properties
        remoteInvoker.start(connectAddress, 1000);

        final ProxyBuilder<InstanceMetadataContext> instanceMetadataContextProxyBuilder = getInstanceMetadataContextProxyBuilderProvider().get();
        final InstanceMetadataContext instanceMetadataContext = instanceMetadataContextProxyBuilder.withRemoteInvoker(remoteInvoker);

        synchronized (atomicInstanceMetadataContexts) {
            final Map<UUID, InstanceMetadataContext> instanceMetadataContexts = atomicInstanceMetadataContexts.get();
            instanceMetadataContexts.put(instanceUuid, instanceMetadataContext);
        }
    }

    private void onInstanceDisconnected(final SrvUniqueIdentifier srvUniqueIdentifier, final UUID instanceUuid) {

    }

    // TODO: we need to manually remove kv-pairs from the most recent load averages whenever an instance goes offline,
    //  otherwise it will stick around forever and therefore would be considered a viable candidate destination for
    //  "ANY" routing.

    @Override
    public void start(long refreshRateMillis) {
        atomicIsRunning.compareAndSet(false, true);

        final Thread thread = new Thread(() -> {
            try {
                boolean isRunning = atomicIsRunning.get();
                while (isRunning && !Thread.interrupted()) {
                    final Map<UUID, Double> loadAverages = new HashMap<>();

                    // TODO: maybe we revisit synchronizing it for so long: since the getLoadAverage calls are blocking,
                    //  we may block the connection established notifier or other accessors of the instance metadata
                    //  contexts
                    synchronized (atomicInstanceMetadataContexts) {
                        final Map<UUID, InstanceMetadataContext> instanceMetadataContexts = atomicInstanceMetadataContexts.get();
                        for (UUID instanceUuid : instanceMetadataContexts.keySet()) {
                            if (!instanceMetadataContexts.containsKey(instanceUuid)) {
                                continue;
                            }
                            final InstanceMetadataContext instanceMetadataContext = instanceMetadataContexts.get(instanceUuid);

                            try {
                                final double loadAverage = instanceMetadataContext.getLoadAverage();
                                loadAverages.put(instanceUuid, loadAverage);
                            }
                            catch (HandlerTimeoutException e) {
                                continue;
                            }
                        }
                    }

                    synchronized (atomicMostRecentLoadAverages) {
                        final BiMap<UUID, Double> allLoadAverages = atomicMostRecentLoadAverages.get();
                        allLoadAverages.putAll(loadAverages);
                    }

                    /**
                     * Right now, we just have a simple thread sleep, regardless of how long it takes for all the
                     * synchronous getLoadAverage() calls to resolve. So in the worst case, we may need to wait up to
                     *      totalMillis = n * remoteInvoker.getTimeoutMillis() + refreshRateMillis, n = the number of
                     *      instances we are polling,
                     * until we do another round of polling.
                     *
                     * TODO: We could do some optimizations here, e.g. trying to parallelize all the getLoadAverage()
                     *  calls (see note above).
                     */
                    Thread.sleep(refreshRateMillis);
                    isRunning = atomicIsRunning.get();
                }
            }
            catch (InterruptedException e) {
                // TODO: should we throw internal exception, stay silent, something else?
            }
        });

        thread.start();
    }

    @Override
    public void stop() {
        atomicIsRunning.compareAndSet(true, false);
    }

    @Override
    public double getMostRecentLoadAverage(final UUID instanceUuid) {
        final Map<UUID, Double> mostRecentLoadAverages = atomicMostRecentLoadAverages.get();

        if (mostRecentLoadAverages.containsKey(instanceUuid)) {
            return mostRecentLoadAverages.get(instanceUuid);
        }
        else {
            return -1;
        }
    }

    @Override
    public UUID getInstanceUuidByOptimalLoadAverage() {
        synchronized (atomicMostRecentLoadAverages) {
            final BiMap<UUID, Double> mostRecentLoadAverages = atomicMostRecentLoadAverages.get();
            final Optional<Double> optimalLoadAverage = mostRecentLoadAverages
                    .values()
                    .stream()
                    .sorted()
                    .findFirst();
            if (optimalLoadAverage.isPresent()) {
                return mostRecentLoadAverages.inverse().get(optimalLoadAverage.get());
            }
            else {
                return null;
            }
        }
    }

    @Override
    public UUID getRandomInstanceUuid() {
        synchronized (atomicMostRecentLoadAverages) {
            final BiMap<UUID, Double> mostRecentLoadAverages = atomicMostRecentLoadAverages.get();
            final Optional<UUID> randomInstanceUuidOptional = mostRecentLoadAverages
                    .keySet()
                    .stream()
                    .skip((int) (mostRecentLoadAverages.size() * Math.random()))
                    .findFirst();
            if (randomInstanceUuidOptional.isPresent()) {
                return randomInstanceUuidOptional.get();
            }
            else {
                return null;
            }
        }
    }

    @Override
    public boolean isRunning() {
        final boolean isRunning = atomicIsRunning.get();
        return isRunning;
    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

    public Provider<ProxyBuilder<InstanceMetadataContext>> getInstanceMetadataContextProxyBuilderProvider() {
        return instanceMetadataContextProxyBuilderProvider;
    }

    @Inject
    public void setInstanceMetadataContextProxyBuilderProvider(Provider<ProxyBuilder<InstanceMetadataContext>> instanceMetadataContextProxyBuilderProvider) {
        this.instanceMetadataContextProxyBuilderProvider = instanceMetadataContextProxyBuilderProvider;
    }
}
