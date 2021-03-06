package com.namazustudios.socialengine.cdnserve.api;

import com.namazustudios.socialengine.dao.DeploymentDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;

import javax.inject.Inject;

public class AnonDeploymentService implements DeploymentService {

    private DeploymentDao deploymentDao;

    @Override
    public Pagination<Deployment> getDeployments(String applicationId, int offset, int count) {
        throw new ForbiddenException();
    }

    @Override
    public Deployment getDeployment(String applicationId, String deploymentId) {
        throw new ForbiddenException();
    }

    @Override
    public Deployment getCurrentDeployment(String applicationId) {
        return getDeploymentDao().getCurrentDeployment(applicationId);
    }

    @Override
    public Deployment updateDeployment(String applicationId, String version, UpdateDeploymentRequest deploymentRequest) {
        throw new ForbiddenException();
    }

    @Override
    public Deployment createDeployment(String applicationId, CreateDeploymentRequest deploymentRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteDeployment(String applicationId, String deploymentId) {
        throw new ForbiddenException();
    }

    public DeploymentDao getDeploymentDao() {
        return deploymentDao;
    }

    @Inject
    public void setDeploymentDao(DeploymentDao deploymentDao) {
        this.deploymentDao = deploymentDao;
    }
}
