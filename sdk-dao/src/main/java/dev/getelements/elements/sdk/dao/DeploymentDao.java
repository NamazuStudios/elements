package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Deployment;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

@ElementServiceExport
public interface DeploymentDao {

    Pagination<Deployment> getDeployments(final String applicationId, final int offset, final int count);

    Pagination<Deployment> getAllDeployments(final int offset, final int count);

    Deployment getDeployment(final String applicationId, final String deploymentId);

    Deployment getCurrentDeployment(final String applicationId);

    Deployment updateDeployment(String applicationId, final Deployment deployment);

    Deployment createDeployment(final Deployment deployment);

    void deleteDeployment(final String applicationId, final String deploymentId);
}
