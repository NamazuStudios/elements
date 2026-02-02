package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.system.ElementDeploymentNotFoundException;
import dev.getelements.elements.sdk.model.system.ElementDeployment;

import java.util.Optional;

/**
 * Provides access to {@link ElementDeployment} instances in the database.
 */
public interface ElementDeploymentDao {

    ElementDeployment createElementDeployment(final ElementDeployment elementDeployment);

    Pagination<ElementDeployment> getElementDeployments(int offset, int count, String search);

    /**
     * Finds the {@link ElementDeployment} with the supplied deployment ID.
     *
     * @param deploymentId the deployment ID
     * @return an {@link Optional<ElementDeployment>}
     */
    Optional<ElementDeployment> findElementDeployment(String deploymentId);

    /**
     * Gets the {@link ElementDeployment} with the supplied deployment ID.
     *
     * @param deploymentId the deployment ID
     * @return the {@link ElementDeployment}, never null
     * @throws ElementDeploymentNotFoundException if not found
     */
    default ElementDeployment getElementDeployment(String deploymentId) {
        return findElementDeployment(deploymentId)
                .orElseThrow(() -> new ElementDeploymentNotFoundException(deploymentId));
    }

    ElementDeployment updateElementDeployment(ElementDeployment elementDeployment);

    void deleteDeployment(String deploymentId);

}
