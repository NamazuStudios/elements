package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoAppleSignInConfiguration;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoProductBundle;
import com.namazustudios.socialengine.exception.application.ApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.IosApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoIosApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.IOS_APP_STORE;
import static java.lang.String.format;

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
    public IosApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final IosApplicationConfiguration iosApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(iosApplicationConfiguration);

        final Query<MongoIosApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoIosApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("category").equal(IOS_APP_STORE),
            query.criteria("uniqueIdentifier").equal(iosApplicationConfiguration.getApplicationId())
        );

        final UpdateOperations<MongoIosApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoIosApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", iosApplicationConfiguration.getApplicationId().trim());
        updateOperations.set("active", true);
        updateOperations.set( "category", iosApplicationConfiguration.getCategory());
        updateOperations.set("parent", mongoApplication);

        if (iosApplicationConfiguration.getProductBundles() != null &&
                iosApplicationConfiguration.getProductBundles().size() > 0) {
            final List<MongoProductBundle> mongoProductBundles = iosApplicationConfiguration
                    .getProductBundles()
                    .stream()
                    .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                    .collect(Collectors.toList());
            updateOperations.set("productBundles", mongoProductBundles);
        }

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
    public IosApplicationConfiguration getIosApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoIosApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoIosApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").equal(IOS_APP_STORE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final MongoIosApplicationConfiguration mongoIosApplicationProfile = query.get();

        if (mongoIosApplicationProfile == null) {
            final String msg = format("application profile %s not found", applicationConfigurationNameOrId);
            throw new ApplicationConfigurationNotFoundException(msg);
        }

        return getBeanMapper().map(mongoIosApplicationProfile, IosApplicationConfiguration.class);

    }

    @Override
    public IosApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final IosApplicationConfiguration iosApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(iosApplicationConfiguration);

        final Query<MongoIosApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoIosApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").equal(IOS_APP_STORE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoIosApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoIosApplicationConfiguration.class);

        final MongoAppleSignInConfiguration mongoAppleSignInConfiguration = getBeanMapper()
            .map(iosApplicationConfiguration.getAppleSignInConfiguration(), MongoAppleSignInConfiguration.class);

        updateOperations.set("uniqueIdentifier", iosApplicationConfiguration.getApplicationId().trim());
        updateOperations.set("category", iosApplicationConfiguration.getCategory());
        updateOperations.set("parent", mongoApplication);
        updateOperations.set("appleSignInConfiguration", mongoAppleSignInConfiguration);

        if (iosApplicationConfiguration.getProductBundles() != null &&
                iosApplicationConfiguration.getProductBundles().size() > 0) {
            final List<MongoProductBundle> mongoProductBundles = iosApplicationConfiguration
                    .getProductBundles()
                    .stream()
                    .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                    .collect(Collectors.toList());
            updateOperations.set("productBundles", mongoProductBundles);
        }
        else {
            updateOperations.unset("productBundles");
        }

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoIosApplicationConfiguration mongoIosApplicationProfile;
        mongoIosApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoIosApplicationProfile);
        return getBeanMapper().map(mongoIosApplicationProfile, IosApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoIosApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoIosApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").equal(IOS_APP_STORE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
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

        switch (psnApplicationProfile.getCategory()) {
            case IOS_APP_STORE:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + psnApplicationProfile.getCategory());
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
