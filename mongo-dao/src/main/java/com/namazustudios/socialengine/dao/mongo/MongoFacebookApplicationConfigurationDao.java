package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoFacebookApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.application.Platform;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.application.Platform.FACEBOOK;

/**
 * Created by patricktwohig on 6/15/17.
 */
public class MongoFacebookApplicationConfigurationDao implements FacebookApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public FacebookApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(facebookApplicationConfiguration);

        final Query<MongoFacebookApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFacebookApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(FACEBOOK),
            query.criteria("uniqueIdentifier").equal(facebookApplicationConfiguration.getApplicationId())
        );

        final UpdateOperations<MongoFacebookApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoFacebookApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId().trim());
        updateOperations.set("active", true);
        updateOperations.set("platform", facebookApplicationConfiguration.getPlatform());
        updateOperations.set("parent", mongoApplication);
        updateOperations.set("applicationSecret", facebookApplicationConfiguration.getApplicationSecret().trim());

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true);

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationProfile;
        mongoFacebookApplicationProfile = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        getObjectIndex().index(mongoFacebookApplicationProfile);
        return getBeanMapper().map(mongoFacebookApplicationProfile, FacebookApplicationConfiguration.class);

    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(
            final String applicationConfigurationNameOrId) {

        final Query<MongoFacebookApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFacebookApplicationConfiguration.class);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("platform").equal(FACEBOOK)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationConfiguration = query.get();

        if (mongoFacebookApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFacebookApplicationConfiguration, FacebookApplicationConfiguration.class);

    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoFacebookApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFacebookApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(FACEBOOK)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationConfiguration = query.get();

        if (mongoFacebookApplicationConfiguration == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFacebookApplicationConfiguration, FacebookApplicationConfiguration.class);

    }

    @Override
    public FacebookApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(facebookApplicationConfiguration);

        final Query<MongoFacebookApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFacebookApplicationConfiguration.class);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("parent").equal(mongoApplication),
                query.criteria("platform").equal(FACEBOOK)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoFacebookApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoFacebookApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId().trim());
        updateOperations.set("platform", facebookApplicationConfiguration.getPlatform());
        updateOperations.set("parent", mongoApplication);
        updateOperations.set("applicationSecret", facebookApplicationConfiguration.getApplicationSecret().trim());

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationProfile;
        mongoFacebookApplicationProfile = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoFacebookApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoFacebookApplicationProfile);
        return getBeanMapper().map(mongoFacebookApplicationProfile, FacebookApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoFacebookApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFacebookApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").equal(FACEBOOK)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final UpdateOperations<MongoFacebookApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoFacebookApplicationConfiguration.class);

        updateOperations.set("active", false);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationProfile;

        mongoFacebookApplicationProfile = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoFacebookApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoFacebookApplicationProfile.getObjectId());
        }

        getObjectIndex().index(mongoFacebookApplicationProfile);

    }

    public void validate(final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        if (facebookApplicationConfiguration == null) {
            throw new InvalidDataException("facebookApplicationConfiguration must not be null.");
        }

        if (facebookApplicationConfiguration.getPlatform() == null) {
            facebookApplicationConfiguration.setPlatform(Platform.FACEBOOK);
        }

        switch (facebookApplicationConfiguration.getPlatform()) {
            case FACEBOOK:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + facebookApplicationConfiguration.getPlatform());
        }

        getValidationHelper().validateModel(facebookApplicationConfiguration);

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
