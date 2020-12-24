package com.namazustudios.socialengine.cdnserve.api;

import com.namazustudios.socialengine.exception.cdnserve.GitCloneIOException;
import com.namazustudios.socialengine.exception.cdnserve.SymbolicLinkIOException;
import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;

public interface DeploymentService {

    Pagination<Deployment> getDeployments(final String applicationId, final int offset, final int count);

    Pagination<Deployment> getAllDeployments(final int offset, final int count);

    Deployment getDeployment(final String applicationId, final String deploymentId);

    Deployment getCurrentDeployment(final String applicationId);

    Deployment createOrUpdateDeployment(final String applicationId, final CreateDeploymentRequest deploymentRequest) throws SymbolicLinkIOException, GitCloneIOException;

    void deleteDeployment(final String applicationId, final String deploymentId) throws SymbolicLinkIOException;
}
