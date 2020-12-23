package com.namazustudios.socialengine.codeserve.api.deploy;

import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;

public interface DeploymentService {

    Pagination<Deployment> getDeployments(final String applicationId, final int offset, final int count);

    Deployment getDeployment(final String applicationId, final String deploymentId);

    Deployment getCurrentDeployment(final String applicationId);

    Deployment createDeployment(final String applicationId, final CreateDeploymentRequest deploymentRequest);

    void deleteDeployment(final String applicationId, final String deploymentId);
}
