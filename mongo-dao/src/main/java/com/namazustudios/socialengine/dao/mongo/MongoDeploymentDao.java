package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.DeploymentDao;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoDeployment;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.cdnserve.DeploymentNotFoundException;
import com.namazustudios.socialengine.exception.cdnserve.DuplicateDeploymentException;
import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Date;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.Sort.descending;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static java.lang.String.format;

public class MongoDeploymentDao implements DeploymentDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public Pagination<Deployment> getDeployments(String applicationId, final int offset, final int count) {
        final var application = getMongoApplicationDao().getActiveMongoApplication(applicationId);
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

        final var application = getMongoApplicationDao().getActiveMongoApplication(applicationId);
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

        final var application = getMongoApplicationDao().getActiveMongoApplication(applicationId);
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

        final var application = getMongoApplicationDao().getActiveMongoApplication(deployment.getApplication().getId());
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

        final var application = getMongoApplicationDao().getActiveMongoApplication(deployment.getApplication().getId());

        final var query = getDatastore().find(MongoDeployment.class);

        final Date nowDate = new Date();
        final var builder = new UpdateBuilder();

        builder.with(
            set("createdAt", nowDate),
            set("application", application),
            set("version", deployment.getVersion()),
            set("revision", deployment.getRevision())
        );

        final var opts = new ModifyOptions().upsert(true).returnDocument(AFTER);
        final var result = getMongoDBUtils().perform(ds -> builder.execute(query, opts));

        if (result == null) {

            final var msg = format("Deployment version: %s, for application: %s, not found",
                    deployment.getVersion(),
                    application.getName());

            throw new DeploymentNotFoundException(msg);

        }

        return getBeanMapper().map(result, Deployment.class);

    }

    @Override
    public void deleteDeployment(String applicationId, String deploymentId) {

        final var application = getMongoApplicationDao().getActiveMongoApplication(applicationId);
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

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }
}
