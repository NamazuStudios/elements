package dev.getelements.elements.rt.remote.watchdog;

import dev.getelements.elements.rt.remote.InstanceConnectionService;
import dev.getelements.elements.rt.remote.Worker;
import org.slf4j.Logger;

import static java.util.stream.Collectors.toSet;

public class NodeBindingWatchdog implements WorkerWatchdog {

    @Override
    public void watch(final Logger logger, final Worker worker) {

        final boolean unhealthy;

        try (var accessor = worker.accessWorkerState()) {

            final var bindings = accessor.getBindingSet()
                .stream()
                .map(InstanceConnectionService.InstanceBinding::getNodeId)
                .collect(toSet());

            unhealthy = accessor
                .getNodeSet()
                .stream()
                .anyMatch(n -> !bindings.contains(n.getNodeId()));

        }

        if (unhealthy) restartUnhealthy(logger, worker);

    }

    private void restartUnhealthy(final Logger logger, final Worker worker) {
        try (var mutator = worker.beginMutation()) {

            final var bindings = mutator.getBindingSet()
                .stream()
                .map(InstanceConnectionService.InstanceBinding::getNodeId)
                .collect(toSet());

            final var unhealthy = mutator.getNodeSet()
                .stream()
                .filter(n -> !bindings.contains(n.getNodeId()))
                .map(n -> n.getNodeId().getApplicationId())
                .collect(toSet());

            unhealthy.forEach(applicationId -> logger.error("Node {} is unhealthy. Restarting.", applicationId));
            mutator.restartNode(unhealthy);
            mutator.commit();

        }
    }


}
