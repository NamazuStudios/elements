package dev.getelements.elements.sdk.service.cdn;

import dev.getelements.elements.sdk.model.Deployment;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.cdn.CreateDeploymentRequest;
import dev.getelements.elements.sdk.model.cdn.UpdateDeploymentRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface CdnDeploymentService {

    String GIT_REPO = "dev.getelements.elements.service.cdn.git.repo";

    Pagination<Deployment> getDeployments(final String applicationId, final int offset, final int count);

    Deployment getDeployment(final String applicationId, final String deploymentId);

    Deployment getCurrentDeployment(final String applicationId);

    Deployment updateDeployment(final String applicationId, String version, final UpdateDeploymentRequest deploymentRequest);

    Deployment createDeployment(final String applicationId, final CreateDeploymentRequest deploymentRequest);

    void deleteDeployment(final String applicationId, final String deploymentId);

}
