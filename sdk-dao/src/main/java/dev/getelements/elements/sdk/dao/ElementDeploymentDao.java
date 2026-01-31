package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.system.ElementDeployment;

/**
 * Provides access to {@link ElementDeployment} instances in the database.
 */
public interface ElementDeploymentDao {

    ElementDeployment createElementDeployment(final ElementDeployment elementDeployment);

    Pagination<ElementDeployment> getElementDeployments(int offset, int count, String search);

    ElementDeployment getElementDeployment(String deploymentId);

    ElementDeployment updateElementDeployment(ElementDeployment elementDeployment);

    void deleteDeployment(String deploymentId);

}
