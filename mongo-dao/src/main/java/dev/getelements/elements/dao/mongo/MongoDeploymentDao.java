package dev.getelements.elements.dao.mongo;

import com.mongodb.DuplicateKeyException;
import dev.getelements.elements.sdk.dao.DeploymentDao;
import dev.getelements.elements.dao.mongo.application.MongoApplicationDao;
import dev.getelements.elements.dao.mongo.model.MongoDeployment;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.cdnserve.DeploymentNotFoundException;
import dev.getelements.elements.sdk.model.exception.cdnserve.DuplicateDeploymentException;
import dev.getelements.elements.sdk.model.Deployment;
import dev.getelements.elements.sdk.model.Pagination;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;

import java.sql.Timestamp;
import java.util.Date;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.Sort.descending;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.String.format;

public class MongoDeploymentDao implements DeploymentDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapperRegistry;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public Pagination<Deployment> getDeployments(String applicationId, final int offset, final int count) {
        final var application = getMongoApplicationDao().getMongoApplication(applicationId);
        final var query = getDatastore().find(MongoDeployment.class);
        query.filter(eq("application", application));
        return getMongoDBUtils().paginationFromQuery(query, offset, count, Deployment.class);
    }

    @Override
    public Pagination<Deployment> getAllDeployments(final int offset, final int count) {
        final var query = getDatastore().find(MongoDeployment.class);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, Deployment.class);
    }

    @Override
    public Deployment getDeployment(final String applicationId, final String deploymentId) {

        final var application = getMongoApplicationDao().getMongoApplication(applicationId);
        final var objectId = getMongoDBUtils().parseOrThrow(deploymentId, DeploymentNotFoundException::new);
        final var query = getDatastore().find(MongoDeployment.class);

        query.filter(
            eq("_id", objectId),
            eq("application", application)
        );

        return getBeanMapper().map(query.first(), Deployment.class);

    }

    @Override
    public Deployment getCurrentDeployment(String applicationId) {

        final var application = getMongoApplicationDao().getMongoApplication(applicationId);
        final var query = getDatastore().find(MongoDeployment.class);

        query.filter(
            eq("application", application)
        );

        final var opts = new FindOptions().sort(descending("createdAt"));
        final var deployment = query.first(opts);
        if (deployment == null) throw new DeploymentNotFoundException("No deployments exist for: " + applicationId);

        return getBeanMapper().map(deployment, Deployment.class);

    }

    @Override
    public Deployment updateDeployment(String applicationId, Deployment deployment) {

        final var application = getMongoApplicationDao().getMongoApplication(deployment.getApplication().getId());
        final var query = getDatastore().find(MongoDeployment.class);

        query.filter(
            eq("version", deployment.getVersion()),
            eq("application", application)
        );

        final var nowDate = new Date();
        final var builder = new UpdateBuilder();

        builder.with(
            set("createdAt", nowDate),
            set("revision", deployment.getRevision())
        );

        final var opts = new ModifyOptions()
            .upsert(false)
            .returnDocument(AFTER);

        final var result = getMongoDBUtils().perform(
            ds -> builder.execute(query, opts),
            DuplicateDeploymentException::new);

        if (result == null) {

            final var msg = format("Deployment version: %s, for application: %s, not found",
                deployment.getVersion(),
                application.getName());

            throw new DeploymentNotFoundException(msg);

        }

        return getBeanMapper().map(result, Deployment.class);

    }

    @Override
    public Deployment createDeployment(Deployment deployment) {
        final var mongoDeployment = getBeanMapper().map(deployment, MongoDeployment.class);
        final var nowDate = new Date();
        final var timestamp = new Timestamp(nowDate.getTime());
        mongoDeployment.setCreatedAt(timestamp);

        try {
            getDatastore().insert(mongoDeployment);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        return getBeanMapper().map(mongoDeployment, Deployment.class);

    }

    @Override
    public void deleteDeployment(String applicationId, String deploymentId) {

        final var application = getMongoApplicationDao().getMongoApplication(applicationId);
        final var objectId = getMongoDBUtils().parseOrThrow(deploymentId, DeploymentNotFoundException::new);
        final var query = getDatastore().find(MongoDeployment.class);

        query.filter(
            eq("_id", objectId),
            eq("application", application)
        );

        final var writeResult = query.delete();

        if (writeResult.getDeletedCount() == 0) {
            throw new NotFoundException("Deployment not found: " + deploymentId);
        } else if (writeResult.getDeletedCount() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }

    }



    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getBeanMapper() {
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }
}
