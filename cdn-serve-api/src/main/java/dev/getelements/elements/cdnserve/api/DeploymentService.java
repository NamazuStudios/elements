package dev.getelements.elements.cdnserve.api;

import dev.getelements.elements.model.Deployment;
import dev.getelements.elements.model.Pagination;

public interface DeploymentService {

    Pagination<Deployment> getDeployments(final String applicationId, final int offset, final int count);

    Deployment getDeployment(final String applicationId, final String deploymentId);

    Deployment getCurrentDeployment(final String applicationId);

    Deployment updateDeployment(final String applicationId, String version, final UpdateDeploymentRequest deploymentRequest);

    Deployment createDeployment(final String applicationId, final CreateDeploymentRequest deploymentRequest);

    void deleteDeployment(final String applicationId, final String deploymentId);
}
