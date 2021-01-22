package com.namazustudios.socialengine.dao.mongo.application;

import com.mongodb.client.result.UpdateResult;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.model.application.ConfigurationCategory;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoFacebookApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import dev.morphia.UpdateOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FACEBOOK;

/**
 * Created by patricktwohig on 6/15/17.
 */
public class MongoFacebookApplicationConfigurationDao implements FacebookApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private Datastore datastore;

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
        query = getDatastore().find(MongoFacebookApplicationConfiguration.class);

        query.filter(Filters.and(
            Filters.eq("active", false),
            Filters.eq("parent", mongoApplication),
            Filters.eq("category", FACEBOOK),
            Filters.eq("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId())
        ));

        final UpdateResult updateResult = query.update(UpdateOperators.set("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId().trim()),
            UpdateOperators.set("active", true),
            UpdateOperators.set("category", facebookApplicationConfiguration.getCategory()),
            UpdateOperators.set("parent", mongoApplication),
            UpdateOperators.set("applicationSecret", facebookApplicationConfiguration.getApplicationSecret().trim())
        ).execute(new UpdateOptions().upsert(true));

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationProfile;
        mongoFacebookApplicationProfile = getMongoDBUtils()
                .perform(ds -> {
                    if(updateResult.getUpsertedId() != null) {
                        return ds.find(MongoFacebookApplicationConfiguration.class)
                                .filter(Filters.eq("_id", updateResult.getUpsertedId())).first();
                    } else {
                        return ds.find(MongoFacebookApplicationConfiguration.class)
                                .filter(Filters.and(
                                        Filters.eq("active", true),
                                        Filters.eq("parent", mongoApplication),
                                        Filters.eq("category", FACEBOOK),
                                        Filters.eq("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId())
                                )).first();
                    }
                });

        getObjectIndex().index(mongoFacebookApplicationProfile);
        return getBeanMapper().map(mongoFacebookApplicationProfile, FacebookApplicationConfiguration.class);

    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(
            final String applicationConfigurationNameOrId) {

        final Query<MongoFacebookApplicationConfiguration> query;
        query = getDatastore().find(MongoFacebookApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("category", FACEBOOK)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationConfiguration = query.first();

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
        query = getDatastore().find(MongoFacebookApplicationConfiguration.class);

        query.filter(Filters.and(
           Filters.eq("active", true),
           Filters.eq("parent", mongoApplication),
           Filters.eq("category", FACEBOOK)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationConfiguration = query.first();

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
        query = getDatastore().find(MongoFacebookApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", FACEBOOK)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationProfileNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationProfileNameOrId));
        }

        query.update(UpdateOperators.set("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId().trim()),
        UpdateOperators.set("category", facebookApplicationConfiguration.getCategory()),
        UpdateOperators.set("parent", mongoApplication),
        UpdateOperators.set("applicationSecret", facebookApplicationConfiguration.getApplicationSecret().trim())
        ).execute(new UpdateOptions().upsert(false));

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationConfiguration;
        mongoFacebookApplicationConfiguration = getMongoDBUtils()
                .perform(ds -> ds.find(MongoFacebookApplicationConfiguration.class)
                        .filter(Filters.and(
                                Filters.eq("active", true),
                                Filters.eq("parent", mongoApplication),
                                Filters.eq("category", FACEBOOK),
                                Filters.eq("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId().trim())
                        )).first()
                );

        if (mongoFacebookApplicationConfiguration == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoFacebookApplicationConfiguration);
        return getBeanMapper().map(mongoFacebookApplicationConfiguration, FacebookApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoFacebookApplicationConfiguration> query;
        query = getDatastore().find(MongoFacebookApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", FACEBOOK)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        query.update(UpdateOperators.set("active", false)).execute(new UpdateOptions().upsert(false));

        final MongoFacebookApplicationConfiguration mongoFacebookApplicationProfile;

        mongoFacebookApplicationProfile = getMongoDBUtils()
                .perform(ds -> {
                    try {
                        return ds.find(MongoFacebookApplicationConfiguration.class)
                                .filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId))).first();
                    } catch (IllegalArgumentException ex) {
                        return ds.find(MongoFacebookApplicationConfiguration.class)
                                .filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId)).first();
                    }

                });

        if (mongoFacebookApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoFacebookApplicationProfile.getObjectId());
        }

        getObjectIndex().index(mongoFacebookApplicationProfile);

    }

    public void validate(final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        if (facebookApplicationConfiguration == null) {
            throw new InvalidDataException("facebookApplicationConfiguration must not be null.");
        }

        if (facebookApplicationConfiguration.getCategory() == null) {
            facebookApplicationConfiguration.setCategory(ConfigurationCategory.FACEBOOK);
        }

        switch (facebookApplicationConfiguration.getCategory()) {
            case FACEBOOK:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + facebookApplicationConfiguration.getCategory());
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

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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
