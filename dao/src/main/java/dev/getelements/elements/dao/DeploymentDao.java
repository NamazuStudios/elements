package dev.getelements.elements.dao;

import dev.getelements.elements.model.Deployment;
import dev.getelements.elements.model.Pagination;

public interface DeploymentDao {

    Pagination<Deployment> getDeployments(final String applicationId, final int offset, final int count);

    Pagination<Deployment> getAllDeployments(final int offset, final int count);

    Deployment getDeployment(final String applicationId, final String deploymentId);

    Deployment getCurrentDeployment(final String applicationId);

    Deployment updateDeployment(String applicationId, final Deployment deployment);

    Deployment createDeployment(final Deployment deployment);

    void deleteDeployment(final String applicationId, final String deploymentId);
}
