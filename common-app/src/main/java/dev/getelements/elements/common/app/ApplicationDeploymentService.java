package dev.getelements.elements.common.app;

import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.application.Application;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Handles the deployment of {@link Element}s defined by {@link Application}s.
 */
public interface ApplicationDeploymentService {

    /**
     * Defines a name for the app-node deployment service.
     */
    String APP_NODE = "dev.getelements.elements.common.app.node";

    /**
     * Defines a name for the app-serve deployment service.
     */
    String APP_SERVE = "dev.getelements.elements.common.app.serve";

    /**
     * Lists all deployments known to the service at the time of the method call.
     *
     * @return a {@link List}
     */
    List<DeploymentRecord> listAllDeployments();

    /**
     * Starts the {@link ApplicationDeploymentService}, deploying all {@link Application}s and loading them into
     * the service.
     */
    List<DeploymentRecord> deployAvailableApplications();

    /**
     * Deploys the {@link Application}.
     *
     * @param application the {@link Application}
     */
    DeploymentRecord deployApplication(Application application);

    /**
     * Aggregates the deployment information for an {@link Application}. This includes the
     * {@link ApplicationElementRecord} as well as any URIs it exposes.
     *
     * @param status the {@link DeploymentStatus}
     * @param applicationElementRecord the {@link ApplicationElementRecord}, may be null if the deployment is not {@link DeploymentStatus#CLEAN}
     * @param logs a set of very brief logs produced during deployment.
     * @param uris the set of URIs exposed by the deployment
     * @param errors a set of {@link java.net.URI}s served out of this deployment
     */
    record DeploymentRecord(
            DeploymentStatus status,
            ApplicationElementRecord applicationElementRecord,
            Set<URI> uris,
            List<String> logs,
            List<Throwable> errors) {

        public DeploymentRecord {
            uris = Set.copyOf(uris);
            logs = List.copyOf(logs);
            errors = List.copyOf(errors);
        }

        /**
         * Creates a {@link DeploymentRecord} indicating an unsuccessful deployment.
         *
         * @param logs the logs produced during deployment
         * @param error the error produced during deployment
         * @return a {@link DeploymentRecord}
         */
        public static DeploymentRecord fail(final List<String> logs, final Throwable error) {
            return new DeploymentRecord(DeploymentStatus.FAILED,
                    null,
                    Set.of(),
                    List.copyOf(logs),
                    List.of(error)
            );
        }

        /**
         * Creates a {@link DeploymentRecord} indicating an unsuccessful deployment.
         *
         * @param logs the logs produced during deployment
         * @param causes the errors produced during deployment
         * @return a {@link DeploymentRecord}
         */
        public static DeploymentRecord fail(final List<String> logs, List<Throwable> causes) {
            return new DeploymentRecord(
                    DeploymentStatus.FAILED,
                    null,
                    Set.of(),
                    List.copyOf(logs),
                    List.copyOf(causes)
            );
        }

    }

    /**
     * Indicates the status of a deployment.
     */
    enum DeploymentStatus {

        /**
         * The deployment has been deployed with success.
         */
        CLEAN,

        /**
         * Indicates that the deployment completed, but there were warnings during the process which resulted in
         * exceptions that were caught and handled.
         */
        UNSTABLE,

        /**
         * The deployment has failed due to one reason or another due to the inability to load the application or
         * its elements.
         */
        FAILED

    }

}
