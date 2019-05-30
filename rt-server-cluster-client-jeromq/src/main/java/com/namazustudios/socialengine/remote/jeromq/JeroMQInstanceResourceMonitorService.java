package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.InstanceResourceMonitorService;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class JeroMQInstanceResourceMonitorService implements InstanceResourceMonitorService {
    private final AtomicBoolean atomicIsRunning = new AtomicBoolean(false);
    private final AtomicReference<Set<UUID>> atomicInstanceUuids = new AtomicReference<>(new HashSet<>());

    // TODO: maybe we record the timestamp as well for when we get the data sample (see TODO notes below)
    private final AtomicReference<Map<UUID, Double>> atomicMostRecentLoadAverages = new AtomicReference<>(new HashMap<>());

    // TODO: we need to manually remove kv-pairs from the most recent load averages whenever an instance goes offline,
    //  otherwise it will stick around forever and therefore would be considered a viable candidate destination for
    //  "ANY" routing.

    @Override
    public void start(long refreshRateMillis) {
        atomicIsRunning.compareAndSet(false, true);

        final Thread thread = new Thread(() -> {
            try {
                boolean isRunning = atomicIsRunning.get();
                while (isRunning) {
                    final Set<UUID> instanceUuids = atomicInstanceUuids.get();
                    final Map<UUID, Double> loadAverages = new HashMap<>();
                    for (UUID instanceUuid : instanceUuids) {
                        // TODO: we need to perform the remote invocation against the InstanceMetadataContext, pointed
                        //  to the correct instance by utilizing the given instanceUuid. For now, we just hardcode -1.
                        // TODO: we need to try/catch the remote invocation and determine the right strategy, e.g. do
                        //  we record the load average for that instance as -1, or do nothing? What happens if we see
                        //  failures over a long period of time?
                        final double loadAverage = -1;

                        loadAverages.put(instanceUuid, loadAverage);
                    }

                    synchronized (atomicMostRecentLoadAverages) {
                        final Map<UUID, Double> allLoadAverages = atomicMostRecentLoadAverages.get();
                        allLoadAverages.putAll(loadAverages);
                    }

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

    }

    @Override
    public UUID getRandomInstanceUuid() {
        final Set<UUID> instanceUuids = atomicInstanceUuids.get();
        final Optional<UUID> randomInstanceUuidOptional = instanceUuids
                .stream()
                .skip((int) (instanceUuids.size() * Math.random()))
                .findFirst();
        if (randomInstanceUuidOptional.isPresent()) {
            return randomInstanceUuidOptional.get();
        }
        else {
            return null;
        }
    }

    @Override
    public boolean isRunning() {
        final boolean isRunning = atomicIsRunning.get();
        return isRunning;
    }

}
