package dev.getelements.elements.sdk.deployment;

import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.system.ElementDeployment;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * The Element Container Service is responsible for managing Element containers.
 * It mounts and unmounts containers in response to runtime deployments.
 *
 * <h3>Container Lifecycle Events</h3>
 * The service publishes the following events:
 * <ul>
 *     <li>{@link #CONTAINER_MOUNTED} - when a container is mounted</li>
 *     <li>{@link #CONTAINER_UNMOUNTED} - when a container is unmounted</li>
 * </ul>
 */
public interface ElementContainerService {

    /**
     * Event published when a container is mounted.
     * Event arguments: deploymentId (String), status (ContainerStatus), record (ContainerRecord)
     */
    String CONTAINER_MOUNTED = "dev.getelements.elements.container.mounted";

    /**
     * Event published when a container is unmounted.
     * Event arguments: deploymentId (String)
     */
    String CONTAINER_UNMOUNTED = "dev.getelements.elements.container.unmounted";

    /**
     * Starts the container service.
     */
    void start();

    /**
     * Stops the container service.
     */
    void stop();

    /**
     * Gets all active containers. This may return a snapshot or a copy of the containers as they are running.
     *
     * @return all active containers
     */
    List<ContainerRecord> getActiveContainers();

    /**
     * Aggregates the deployment information for an {@link Application}. This includes the
     * {@link ElementDeployment} as well as any URIs it exposes.
     *
     * @param status the {@link ContainerRecord}
     * @param runtime the {@link RuntimeRecord}
     * @param logs a set of very brief logs produced during deployment.
     * @param uris the set of URIs exposed by the deployment
     * @param errors a set of {@link java.net.URI}s served out of this deployment
     */
    record ContainerRecord(
            RuntimeRecord runtime,
            ContainerStatus status,
            Set<URI> uris,
            List<String> logs,
            List<Throwable> errors,
            List<Element> elements) {

        public ContainerRecord {
            uris = uris == null ? Set.of() : Set.copyOf(uris);
            logs = logs == null ? List.of() : List.copyOf(logs);
            errors = errors == null ? List.of() : List.copyOf(errors);
            elements = elements == null ? List.of() : List.copyOf(elements);
        }

        /**
         * Creates a {@link ContainerRecord} indicating an unsuccessful deployment.
         *
         * @param logs the logs produced during deployment
         * @param error the error produced during deployment
         * @return a {@link ContainerRecord}
         */
        public static ContainerRecord fail(
                final RuntimeRecord runtime,
                final List<String> logs,
                final Throwable error) {
            return new ContainerRecord(
                    runtime,
                    ContainerStatus.FAILED,
                    null,
                    List.copyOf(logs),
                    List.of(error),
                    List.of()
            );
        }

        /**
         * Creates a {@link ContainerRecord} indicating an unsuccessful deployment.
         *
         * @param logs the logs produced during deployment
         * @param causes the errors produced during deployment
         * @return a {@link ContainerRecord}
         */
        public static ContainerRecord fail(
                final RuntimeRecord runtime,
                final List<String> logs,
                final List<Throwable> causes) {
            return new ContainerRecord(
                    runtime,
                    ContainerStatus.FAILED,
                    Set.of(),
                    List.copyOf(logs),
                    List.copyOf(causes),
                    List.of()
            );
        }

    }

    /**
     * Indicates the status of a deployment.
     */
    enum ContainerStatus {

        /**
         * The container has been deployed with success.
         */
        CLEAN,

        /**
         * Indicates that the container completed, but there were warnings during the process.
         */
        WARNINGS,

        /**
         * Indicates that the container completed, but there were warnings during the process which resulted in
         * exceptions that were caught and handled.
         */
        UNSTABLE,

        /**
         * The container has failed due to one reason or another due to the inability to load the application or
         * its elements.
         */
        FAILED

    }

}
