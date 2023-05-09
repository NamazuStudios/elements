package dev.getelements.elements.rt.remote.watchdog;

import dev.getelements.elements.rt.remote.Worker;
import org.slf4j.Logger;

import static dev.getelements.elements.rt.remote.NodeState.UNHEALTHY;
import static java.util.stream.Collectors.toSet;

public class NodeHealthWorkerWatchdog implements WorkerWatchdog {

    @Override
    public void watch(final Logger logger, final Worker worker) {

        final boolean unhealthy;

        try (var accessor = worker.accessWorkerState()) {
            unhealthy = accessor
                .getNodeSet()
                .stream()
                .anyMatch(node -> UNHEALTHY.equals(node.getState()));
        }

        if (unhealthy) restartUnhealthy(logger, worker);

    }

    private void restartUnhealthy(final Logger logger, final Worker worker) {
        try (var mutator = worker.beginMutation()) {

            final var unhealthy = mutator.getNodeSet()
                .stream()
                .filter(node -> UNHEALTHY.equals(node.getState()))
                .map(n -> n.getNodeId().getApplicationId())
                .collect(toSet());

            unhealthy.forEach(applicationId -> logger.error("Node {} is unhealthy. Restarting.", applicationId));
            mutator.restartNode(unhealthy);
            mutator.commit();

        }
    }

}
