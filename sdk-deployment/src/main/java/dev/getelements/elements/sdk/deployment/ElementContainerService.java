package dev.getelements.elements.sdk.deployment;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
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
@ElementPublic
@ElementEventProducer(
        value = ElementContainerService.CONTAINER_MOUNTED,
        parameters = {String.class, ElementContainerService.ContainerStatus.class, ElementContainerService.ContainerRecord.class},
        description = "Published when a container is mounted. Arguments: deploymentId, status, record"
)
@ElementEventProducer(
        value = ElementContainerService.CONTAINER_UNMOUNTED,
        parameters = String.class,
        description = "Published when a container is unmounted. Arguments: deploymentId"
)
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
     * Defines an attribute which specifies the prefix for the element. At laod-time, loader will inspect the
     * {@link Attributes} from the {@link Element}. If blank ({@see {@link String#isBlank()}}, then the loader will
     * defer to the value of {@link ElementDefinition#value()}, which would typically be the name of the package
     * bearing the annotation.
     */
    @ElementDefaultAttribute(description = "The prefix for the application. If blank, the element name will be used.")
    String APPLICATION_PREFIX = "dev.getelements.elements.app.serve.prefix";

    /**
     * Defines an attribute which specifies if the elements should enable the standard auth pipeline in Elements.
     * This ensures that the application server will be able to authenticate users using the Authorization or
     * Elements-SessionSecret headers as well as allow the appropriate override headers to be used.
     */
    @ElementDefaultAttribute(
            value = "false",
            description = "Set to 'true' to enable the standard Elements authentication pipeline."
    )
    String ENABLE_ELEMENTS_AUTH = "dev.getelements.elements.auth.enabled";

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
