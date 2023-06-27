package dev.getelements.elements.cdnserve.api;

import dev.getelements.elements.dao.DeploymentDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Deployment;
import dev.getelements.elements.model.Pagination;

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
