package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.system.ElementDeploymentNotFoundException;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;

import java.util.List;
import java.util.Optional;

/**
 * Provides access to {@link ElementDeployment} instances in the database.
 */
@ElementServiceExport
public interface ElementDeploymentDao {

    /**
     * Creates a new {@link ElementDeployment}.
     *
     * @param elementDeployment the {@link ElementDeployment}
     * @return the {@link ElementDeployment} as written to the database
     */
    ElementDeployment createElementDeployment(final ElementDeployment elementDeployment);

    /**
     * Gets all deployments with offset, count, and search query.
     *
     * @param offset the offset
     * @param count the count
     * @param search the search
     * @return the {@link Pagination} of {@link ElementDeployment} instances
     */
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

    /**
     * Updates an {@link ElementDeployment}. Note this must implicitly update the {@link ElementDeployment#version()}
     * value incrementing by one for every successful modification.
     *
     * @param elementDeployment the deployment
     * @return the updated deployment
     */
    ElementDeployment updateElementDeployment(ElementDeployment elementDeployment);

    /**
     * Deletes the {@link ElementDeployment} with the supplied id.
     *
     * @param deploymentId the deployment ID
     */
    void deleteDeployment(String deploymentId);

    /**
     * Gets all {@link ElementDeployment} instances with the specified state.
     *
     * @param state the state to filter by
     * @return list of deployments in that state
     */
    List<ElementDeployment> getElementDeploymentsByState(ElementDeploymentState state);

}
