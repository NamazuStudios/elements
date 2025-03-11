package dev.getelements.elements.service.cdn;

import dev.getelements.elements.sdk.dao.DeploymentDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.Deployment;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.cdn.CreateDeploymentRequest;
import dev.getelements.elements.sdk.model.cdn.UpdateDeploymentRequest;

import dev.getelements.elements.sdk.service.cdn.CdnDeploymentService;
import jakarta.inject.Inject;

public class AnonCdnDeploymentService implements CdnDeploymentService {

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
