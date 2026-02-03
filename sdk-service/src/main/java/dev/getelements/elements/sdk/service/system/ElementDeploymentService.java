package dev.getelements.elements.sdk.service.system;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.system.CreateElementDeploymentRequest;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.UpdateElementDeploymentRequest;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages {@link ElementDeployment} instances in the database.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ElementDeploymentService {

    /**
     * Creates an {@link ElementDeployment} with the supplied request.
     *
     * @param request the request
     * @return the {@link ElementDeployment}
     */
    ElementDeployment createElementDeployment(CreateElementDeploymentRequest request);

    /**
     * Gets all available {@link ElementDeployment}s.
     *
     * @param offset the offset
     * @param count the count
     * @param search the search query
     * @return a {@link Pagination} of {@link ElementDeployment} instances
     */
    Pagination<ElementDeployment> getElementDeployments(int offset, int count, String search);

    /**
     * Gets the {@link ElementDeployment}.
     *
     * @param deploymentId the deployment ID
     * @return the {@link ElementDeployment}
     */
    ElementDeployment getElementDeployment(String deploymentId);

    /**
     * Updates an existing {@link ElementDeployment}.
     *
     * @param deploymentId the deployment id
     * @param deploymentRequest the request
     *
     * @return the {@link ElementDeployment}
     */
    ElementDeployment updateElementDeployment(String deploymentId, UpdateElementDeploymentRequest deploymentRequest);

    /**
     * Deletes an {@link ElementDeployment}.
     *
     * @param deploymentId the deployment id
     */
    void deleteDeployment(String deploymentId);

}
