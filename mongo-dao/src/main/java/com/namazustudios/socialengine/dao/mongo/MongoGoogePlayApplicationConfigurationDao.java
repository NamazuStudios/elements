package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.GooglePlayApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoGooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.application.Platform.ANDROID_GOOGLE_PLAY;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoGoogePlayApplicationConfigurationDao implements GooglePlayApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public GooglePlayApplicationConfiguration createOrUpdateInactiveApplicationProfile(
            final String applicationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationProfile) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(googlePlayApplicationProfile);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoGooglePlayApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(ANDROID_GOOGLE_PLAY),
            query.criteria("name").equal(googlePlayApplicationProfile.getApplicationId())
        );

        final UpdateOperations<MongoGooglePlayApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoGooglePlayApplicationConfiguration.class);

        updateOperations.set("name", googlePlayApplicationProfile.getApplicationId().trim());
        updateOperations.set("active", true);
        updateOperations.set("platform", googlePlayApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(true);

        final MongoGooglePlayApplicationConfiguration mongoGooglePlayApplicationProfile;
        mongoGooglePlayApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        getObjectIndex().index(mongoGooglePlayApplicationProfile);
        return getBeanMapper().map(mongoGooglePlayApplicationProfile, GooglePlayApplicationConfiguration.class);

    }

    @Override
    public GooglePlayApplicationConfiguration getGooglePlayApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoGooglePlayApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(ANDROID_GOOGLE_PLAY)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final MongoGooglePlayApplicationConfiguration mongoIosApplicationProfile = query.get();

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationProfileNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoIosApplicationProfile, GooglePlayApplicationConfiguration.class);

    }

    @Override
    public GooglePlayApplicationConfiguration updateApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationProfile) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(googlePlayApplicationProfile);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoGooglePlayApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(ANDROID_GOOGLE_PLAY)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoGooglePlayApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoGooglePlayApplicationConfiguration.class);

        updateOperations.set("name", googlePlayApplicationProfile.getApplicationId().trim());
        updateOperations.set("platform", googlePlayApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoGooglePlayApplicationConfiguration mongoGooglePlayApplicationProfile;
        mongoGooglePlayApplicationProfile = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoGooglePlayApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoGooglePlayApplicationProfile);
        return getBeanMapper().map(mongoGooglePlayApplicationProfile, GooglePlayApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoGooglePlayApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(ANDROID_GOOGLE_PLAY)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoGooglePlayApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoGooglePlayApplicationConfiguration.class);

        updateOperations.set("active", false);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoGooglePlayApplicationConfiguration mongoGooglePlayApplicationProfile;

        mongoGooglePlayApplicationProfile = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoGooglePlayApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoGooglePlayApplicationProfile.getObjectId());
        }

        getObjectIndex().index(mongoGooglePlayApplicationProfile);

    }

    public void validate(final GooglePlayApplicationConfiguration psnApplicationProfile) {

        if (psnApplicationProfile == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (psnApplicationProfile.getPlatform()) {
            case ANDROID_GOOGLE_PLAY:
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
