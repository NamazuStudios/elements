package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoCommandException;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.IosApplicationProfileDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoIosApplicationProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoPSNApplicationProfile;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.IosApplicationProfile;
import com.namazustudios.socialengine.model.application.Platform;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoIosApplicationProfileDao implements IosApplicationProfileDao {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    @Override
    public IosApplicationProfile createOrUpdateInactiveApplicationProfile(
            final String applicationNameOrId,
            final IosApplicationProfile iosApplicationProfile) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(iosApplicationProfile);

        final Query<MongoPSNApplicationProfile> query;
        query = getDatastore().createQuery(MongoPSNApplicationProfile.class);

        query.and(
                query.criteria("active").equal(false),
                query.criteria("parent").equal(mongoApplication),
                query.criteria("platform").equal(Platform.IOS_APP_STORE),
                query.criteria("name").equal(iosApplicationProfile.getApplicationId())
        );

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("name", iosApplicationProfile.getApplicationId().trim());
        updateOperations.set("active", true);
        updateOperations.set("platform", iosApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        try {

            final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true);

            mongoPSNApplicationProfile = getDatastore().findAndModify(query, updateOperations, findAndModifyOptions);

        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        objectIndex.index(mongoPSNApplicationProfile);
        return getBeanMapper().map(mongoPSNApplicationProfile, IosApplicationProfile.class);

    }

    @Override
    public IosApplicationProfile getIosApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final Query<MongoIosApplicationProfile> query = getDatastore().createQuery(MongoIosApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent =", mongoApplication);
        query.filter("platform = ", Platform.IOS_APP_STORE);

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final MongoIosApplicationProfile mongoIosApplicationProfile = query.get();

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationProfileNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoIosApplicationProfile, IosApplicationProfile.class);

    }

    @Override
    public IosApplicationProfile updateApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final IosApplicationProfile iosApplicationProfile) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(iosApplicationProfile);

        final Query<MongoIosApplicationProfile> query;
        query = getDatastore().createQuery(MongoIosApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent =", mongoApplication);
        query.filter("platform =", Platform.IOS_APP_STORE);

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoIosApplicationProfile> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoIosApplicationProfile.class);

        updateOperations.set("name", iosApplicationProfile.getApplicationId().trim());
        updateOperations.set("platform", iosApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final MongoIosApplicationProfile mongoIosApplicationProfile;

        try {

            final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true);

            mongoIosApplicationProfile = getDatastore().findAndModify(query, updateOperations, findAndModifyOptions);

        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(iosApplicationProfile);
        return getBeanMapper().map(iosApplicationProfile, IosApplicationProfile.class);

    }

    @Override
    public void softDeleteApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoIosApplicationProfile> query;
        query = getDatastore().createQuery(MongoIosApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent =", mongoApplication);
        query.filter("platform =", Platform.ANDROID_GOOGLE_PLAY);

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoIosApplicationProfile> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoIosApplicationProfile.class);

        updateOperations.set("active", false);

        final MongoIosApplicationProfile mongoIosApplicationProfile;

        try {

            final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                    .returnNew(true)
                    .upsert(false);

            mongoIosApplicationProfile = getDatastore().findAndModify(query, updateOperations, findAndModifyOptions);

        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoIosApplicationProfile.getObjectId());
        }

        getObjectIndex().index(mongoIosApplicationProfile);

    }

    public void validate(final IosApplicationProfile psnApplicationProfile) {

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

}
