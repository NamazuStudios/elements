package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;

public interface DeploymentDao {

    Pagination<Deployment> getDeployments(final String applicationId, final int offset, final int count);

    Deployment getDeployment(final String applicationId, final String deploymentId);

    Deployment getCurrentDeployment(final String applicationId);

    Deployment createDeployment(final String applicationId, final Deployment deployment);

    void deleteDeployment(final String applicationId, final String deploymentId);
}
