package dev.getelements.elements.common.app;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.application.Application;

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
     * Starts the {@link ApplicationDeploymentService}, deploying all {@link Application}s and loading them into
     * the service.
     */
    void deployAvailableApplications();

    /**
     * Deploys the {@link Application}.
     *
     * @param application the {@link Application}
     */
    void deployApplication(Application application);

}
