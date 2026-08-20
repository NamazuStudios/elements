package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Deployment;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

@ElementServiceExport
@ElementEventProducer(
        value = DeploymentDao.DEPLOYMENT_CREATED,
        parameters = Deployment.class,
        description = "Called when a deployment was created."
)
@ElementEventProducer(
        value = DeploymentDao.DEPLOYMENT_CREATED,
        parameters = {Deployment.class, Transaction.class},
        description = "Called when a deployment was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = DeploymentDao.DEPLOYMENT_UPDATED,
        parameters = Deployment.class,
        description = "Called when a deployment was updated."
)
@ElementEventProducer(
        value = DeploymentDao.DEPLOYMENT_UPDATED,
        parameters = {Deployment.class, Transaction.class},
        description = "Called when a deployment was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = DeploymentDao.DEPLOYMENT_DELETED,
        parameters = Deployment.class,
        description = "Called when a deployment was deleted."
)
@ElementEventProducer(
        value = DeploymentDao.DEPLOYMENT_DELETED,
        parameters = {Deployment.class, Transaction.class},
        description = "Called when a deployment was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface DeploymentDao {

    String DEPLOYMENT_CREATED = "dev.getelements.elements.sdk.model.dao.deployment.created";

    String DEPLOYMENT_UPDATED = "dev.getelements.elements.sdk.model.dao.deployment.updated";

    String DEPLOYMENT_DELETED = "dev.getelements.elements.sdk.model.dao.deployment.deleted";

    Pagination<Deployment> getDeployments(final String applicationId, final int offset, final int count);

    Pagination<Deployment> getAllDeployments(final int offset, final int count);

    Deployment getDeployment(final String applicationId, final String deploymentId);

    Deployment getCurrentDeployment(final String applicationId);

    Deployment updateDeployment(String applicationId, final Deployment deployment);

    Deployment createDeployment(final Deployment deployment);

    void deleteDeployment(final String applicationId, final String deploymentId);
}
