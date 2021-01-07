package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.WriteResult;
import com.namazustudios.socialengine.dao.DeploymentDao;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoDeployment;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.application.ApplicationNotFoundException;
import com.namazustudios.socialengine.exception.cdnserve.DeploymentNotFoundException;
import com.namazustudios.socialengine.exception.cdnserve.DuplicateDeploymentException;
import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.util.Date;

public class MongoDeploymentDao implements DeploymentDao {

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private Mapper beanMapper;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public Pagination<Deployment> getDeployments(String applicationId, final int offset, final int count) {
        final MongoApplication application = getMongoApplicationDao().findActiveMongoApplication(applicationId);
        if(application == null){
            throw new ApplicationNotFoundException("Application not found with Id: " + applicationId);
        }
        final Query<MongoDeployment> query = getDatastore().createQuery(MongoDeployment.class);

        query.criteria("application").equal(application);

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> getBeanMapper().map(input, Deployment.class));
    }

    @Override
    public Pagination<Deployment> getAllDeployments(final int offset, final int count) {
        final Query<MongoDeployment> query = getDatastore().createQuery(MongoDeployment.class);

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> getBeanMapper().map(input, Deployment.class));
    }

    @Override
    public Deployment getDeployment(String applicationId, String deploymentId) {
        final MongoApplication application = getMongoApplicationDao().findActiveMongoApplication(applicationId);
        if(application == null){
            throw new ApplicationNotFoundException("Application not found with Id: " + applicationId);
        }
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(deploymentId);
        final Query<MongoDeployment> query = getDatastore().createQuery(MongoDeployment.class);

        query.and(
                query.criteria("_id").equal(objectId),
                query.criteria("application").equal(application)
        );

        return getBeanMapper().map(query.get(), Deployment.class);
    }

    @Override
    public Deployment getCurrentDeployment(String applicationId) {
        final MongoApplication application = getMongoApplicationDao().findActiveMongoApplication(applicationId);
        if(application == null){
            throw new ApplicationNotFoundException("Application not found with Id: " + applicationId);
        }
        final Query<MongoDeployment> query = getDatastore().createQuery(MongoDeployment.class);

        query.criteria("application").equal(application);

        query.order(Sort.descending("createdAt"));

        return getBeanMapper().map(query.get(), Deployment.class);
    }

    @Override
    public Deployment updateDeployment(String applicationId, Deployment deployment) {
        final MongoApplication application = getMongoApplicationDao().findActiveMongoApplication(deployment.getApplication().getId());
        if(application == null){
            throw new ApplicationNotFoundException("Application not found with Id: " + deployment.getApplication().getId());
        }

        final Query<MongoDeployment> query = getDatastore().createQuery(MongoDeployment.class);

        query.and(
                query.criteria("version").equal(deployment.getVersion()),
                query.criteria("application").equal(application)
        );

        if(query.get() == null){
            throw new DeploymentNotFoundException(String.format("Deployment version: %s, for application: %s, not found", deployment.getVersion(), application.getName()));
        }

        final UpdateOperations<MongoDeployment> updateOperations;

        updateOperations = getDatastore().createUpdateOperations(MongoDeployment.class);
        updateOperations.set("revision", deployment.getRevision());
        final Date nowDate = new Date();
        updateOperations.set("createdAt", nowDate);

        final MongoDeployment mongoDeployment = getDatastore().findAndModify(query, updateOperations, new FindAndModifyOptions().upsert(true).returnNew(true));

        return getBeanMapper().map(mongoDeployment, Deployment.class);
    }

    @Override
    public Deployment createDeployment(Deployment deployment) {
        final MongoApplication application = getMongoApplicationDao().findActiveMongoApplication(deployment.getApplication().getId());
        if(application == null){
            throw new ApplicationNotFoundException("Application not found with Id: " + deployment.getApplication().getId());
        }

        final Query<MongoDeployment> query = getDatastore().createQuery(MongoDeployment.class);

        query.criteria("version").equal(deployment.getVersion());

        MongoDeployment res = query.get();
        if(res != null && res.getVersion().equals(deployment.getVersion())) {
            throw new DuplicateDeploymentException(String.format("Deployment version: %s, already exists, suggest changing version or updating existing version", deployment.getVersion()));
        }

        final UpdateOperations<MongoDeployment> updateOperations;

        updateOperations = getDatastore().createUpdateOperations(MongoDeployment.class);
        updateOperations.set("version", deployment.getVersion());
        updateOperations.set("revision", deployment.getRevision());
        updateOperations.set("application", application);
        final Date nowDate = new Date();
        updateOperations.set("createdAt", nowDate);

        final MongoDeployment mongoDeployment = getDatastore().findAndModify(query, updateOperations, new FindAndModifyOptions().upsert(true).returnNew(true));

        return getBeanMapper().map(mongoDeployment, Deployment.class);
    }

    @Override
    public void deleteDeployment(String applicationId, String deploymentId) {
        final MongoApplication application = getMongoApplicationDao().findActiveMongoApplication(applicationId);
        if(application == null){
            throw new ApplicationNotFoundException("Application not found with Id: " + applicationId);
        }
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(deploymentId);
        final Query<MongoDeployment> query = getDatastore().createQuery(MongoDeployment.class);

        query.and(
                query.criteria("_id").equal(objectId),
                query.criteria("application").equal(application)
        );

        final WriteResult writeResult = getDatastore().delete(query);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Deployment not found: " + deploymentId);
        } else if (writeResult.getN() > 1) {
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

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
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
