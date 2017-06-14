package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.IosApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoIosApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.application.Platform.IOS_APP_STORE;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoIosApplicationConfigurationDao implements IosApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public IosApplicationConfiguration createOrUpdateInactiveApplicationProfile(
            final String applicationNameOrId,
            final IosApplicationConfiguration iosApplicationProfile) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(iosApplicationProfile);

        final Query<MongoIosApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoIosApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(IOS_APP_STORE),
            query.criteria("name").equal(iosApplicationProfile.getApplicationId())
        );

        final UpdateOperations<MongoIosApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoIosApplicationConfiguration.class);

        updateOperations.set("name", iosApplicationProfile.getApplicationId().trim());
        updateOperations.set("active", true);
        updateOperations.set("platform", iosApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(true);

        final MongoIosApplicationConfiguration mongoIosApplicationProfile;

        mongoIosApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        getObjectIndex().index(mongoIosApplicationProfile);
        return getBeanMapper().map(mongoIosApplicationProfile, IosApplicationConfiguration.class);

    }

    @Override
    public IosApplicationConfiguration getIosApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoIosApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoIosApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(IOS_APP_STORE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final MongoIosApplicationConfiguration mongoIosApplicationProfile = query.get();

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationProfileNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoIosApplicationProfile, IosApplicationConfiguration.class);

    }

    @Override
    public IosApplicationConfiguration updateApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final IosApplicationConfiguration iosApplicationProfile) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(iosApplicationProfile);

        final Query<MongoIosApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoIosApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(IOS_APP_STORE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoIosApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoIosApplicationConfiguration.class);

        updateOperations.set("name", iosApplicationProfile.getApplicationId().trim());
        updateOperations.set("platform", iosApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoIosApplicationConfiguration mongoIosApplicationProfile;
        mongoIosApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(iosApplicationProfile);
        return getBeanMapper().map(iosApplicationProfile, IosApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoIosApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoIosApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(IOS_APP_STORE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoIosApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoIosApplicationConfiguration.class);

        updateOperations.set("active", false);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoIosApplicationConfiguration mongoIosApplicationProfile;

        mongoIosApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoIosApplicationProfile.getObjectId());
        }

        getObjectIndex().index(mongoIosApplicationProfile);

    }

    public void validate(final IosApplicationConfiguration psnApplicationProfile) {

        if (psnApplicationProfile == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (psnApplicationProfile.getPlatform()) {
            case IOS_APP_STORE:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + psnApplicationProfile.getPlatform());
        }

        getValidationHelper().validateModel(psnApplicationProfile);

    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

}
