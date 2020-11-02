package com.namazustudios.socialengine.dao.mongo.application;

import com.google.inject.internal.cglib.core.$CollectionUtils;
import com.mongodb.client.result.UpdateResult;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoProductBundle;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.GooglePlayApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoGooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import dev.morphia.UpdateOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.ANDROID_GOOGLE_PLAY;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoGooglePlayApplicationConfigurationDao implements GooglePlayApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public GooglePlayApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(googlePlayApplicationConfiguration);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().find(MongoGooglePlayApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", false),
                Filters.eq("parent", mongoApplication),
                Filters.eq( "category", ANDROID_GOOGLE_PLAY),
                Filters.eq("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId())
        ));

        List<MongoProductBundle> mongoProductBundles = null;
        if (googlePlayApplicationConfiguration.getProductBundles() != null &&
                googlePlayApplicationConfiguration.getProductBundles().size() > 0) {
            mongoProductBundles = googlePlayApplicationConfiguration
                    .getProductBundles()
                    .stream()
                    .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                    .collect(Collectors.toList());
        }

        UpdateResult updateResult;
        if (googlePlayApplicationConfiguration.getJsonKey() != null && mongoProductBundles != null) {
            updateResult = query.update(UpdateOperators.set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                UpdateOperators.set("active", true),
                UpdateOperators.set( "category", googlePlayApplicationConfiguration.getCategory()),
                UpdateOperators.set("parent", mongoApplication),
                UpdateOperators.set("jsonKey", googlePlayApplicationConfiguration.getJsonKey()),
                UpdateOperators.set("productBundles", mongoProductBundles)
            ).execute(new UpdateOptions().upsert(true));
        }
        else if(googlePlayApplicationConfiguration.getJsonKey() != null){
            updateResult = query.update(UpdateOperators.set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                    UpdateOperators.set("active", true),
                    UpdateOperators.set( "category", googlePlayApplicationConfiguration.getCategory()),
                    UpdateOperators.set("parent", mongoApplication),
                    UpdateOperators.set("jsonKey", googlePlayApplicationConfiguration.getJsonKey())
            ).execute(new UpdateOptions().upsert(true));
        }
        else if(mongoProductBundles != null){
            updateResult = query.update(UpdateOperators.set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                    UpdateOperators.set("active", true),
                    UpdateOperators.set( "category", googlePlayApplicationConfiguration.getCategory()),
                    UpdateOperators.set("parent", mongoApplication),
                    UpdateOperators.set("productBundles", mongoProductBundles)
            ).execute(new UpdateOptions().upsert(true));
        }
        else{
            updateResult = query.update(UpdateOperators.set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                    UpdateOperators.set("active", true),
                    UpdateOperators.set( "category", googlePlayApplicationConfiguration.getCategory()),
                    UpdateOperators.set("parent", mongoApplication)
            ).execute(new UpdateOptions().upsert(true));
        }

        final MongoGooglePlayApplicationConfiguration mongoGooglePlayApplicationProfile;
        mongoGooglePlayApplicationProfile = getMongoDBUtils()
            .perform(ds -> {
                if(updateResult.getUpsertedId() != null){
                    return ds.find(MongoGooglePlayApplicationConfiguration.class)
                            .filter(Filters.eq("_id", updateResult.getUpsertedId())).first();
                } else {
                    return ds.find(MongoGooglePlayApplicationConfiguration.class)
                            .filter(Filters.and(
                                    Filters.eq("active", true),
                                    Filters.eq("parent", mongoApplication),
                                    Filters.eq( "category", ANDROID_GOOGLE_PLAY),
                                    Filters.eq("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId())
                            )).first();
                }
            });

        getObjectIndex().index(mongoGooglePlayApplicationProfile);
        return getBeanMapper().map(mongoGooglePlayApplicationProfile, GooglePlayApplicationConfiguration.class);

    }

    @Override
    public GooglePlayApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().find(MongoGooglePlayApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq( "category", ANDROID_GOOGLE_PLAY)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoGooglePlayApplicationConfiguration mongoIosApplicationProfile = query.first();

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoIosApplicationProfile, GooglePlayApplicationConfiguration.class);

    }

    @Override
    public GooglePlayApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(googlePlayApplicationConfiguration);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().find(MongoGooglePlayApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq( "category" ,ANDROID_GOOGLE_PLAY)
        ));

        try {
            query.filter(Filters.eq("_id = ", new ObjectId(applicationProfileNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier = ", applicationProfileNameOrId));
        }

        List<MongoProductBundle> mongoProductBundles = null;
        if (googlePlayApplicationConfiguration.getProductBundles() != null &&
                googlePlayApplicationConfiguration.getProductBundles().size() > 0) {
            mongoProductBundles = googlePlayApplicationConfiguration
                    .getProductBundles()
                    .stream()
                    .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                    .collect(Collectors.toList());
        }

        if (googlePlayApplicationConfiguration.getJsonKey() != null && mongoProductBundles != null) {
            query.update(UpdateOperators.set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                    UpdateOperators.set( "category", googlePlayApplicationConfiguration.getCategory()),
                    UpdateOperators.set("parent", mongoApplication),
                    UpdateOperators.set("jsonKey", googlePlayApplicationConfiguration.getJsonKey()),
                    UpdateOperators.set("productBundles", mongoProductBundles)
            ).execute(new UpdateOptions().upsert(false));
        }
        else if(googlePlayApplicationConfiguration.getJsonKey() != null){
            query.update(UpdateOperators.set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                    UpdateOperators.set( "category", googlePlayApplicationConfiguration.getCategory()),
                    UpdateOperators.set("parent", mongoApplication),
                    UpdateOperators.set("jsonKey", googlePlayApplicationConfiguration.getJsonKey()),
                    UpdateOperators.unset("productBundles")
            ).execute(new UpdateOptions().upsert(false));
        }
        else if(mongoProductBundles != null){
            query.update(UpdateOperators.set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                    UpdateOperators.set( "category", googlePlayApplicationConfiguration.getCategory()),
                    UpdateOperators.set("parent", mongoApplication),
                    UpdateOperators.set("productBundles", mongoProductBundles),
                    UpdateOperators.unset("jsonKey")
            ).execute(new UpdateOptions().upsert(false));
        }
        else{
            query.update(UpdateOperators.set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                    UpdateOperators.set( "category", googlePlayApplicationConfiguration.getCategory()),
                    UpdateOperators.set("parent", mongoApplication)
            ).execute(new UpdateOptions().upsert(false));
        }

        final MongoGooglePlayApplicationConfiguration mongoGooglePlayApplicationProfile;
        mongoGooglePlayApplicationProfile = getMongoDBUtils()
                .perform(ds -> query.first());

        if (mongoGooglePlayApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoGooglePlayApplicationProfile);
        return getBeanMapper().map(mongoGooglePlayApplicationProfile, GooglePlayApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().find(MongoGooglePlayApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq( "category", ANDROID_GOOGLE_PLAY)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        query.update(UpdateOperators.set("active", false)).execute(new UpdateOptions().upsert(false));

        final MongoGooglePlayApplicationConfiguration mongoGooglePlayApplicationProfile;

        mongoGooglePlayApplicationProfile = getMongoDBUtils()
                .perform(ds -> {
                    final Query<MongoGooglePlayApplicationConfiguration> qry = ds.find(MongoGooglePlayApplicationConfiguration.class);
                    try {
                        qry.filter(Filters.and(
                                Filters.eq("active", true),
                                Filters.eq("parent", mongoApplication),
                                Filters.eq( "category", ANDROID_GOOGLE_PLAY),
                                Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId))
                        ));
                    } catch (IllegalArgumentException ex) {
                        qry.filter(Filters.and(
                                Filters.eq("active", true),
                                Filters.eq("parent", mongoApplication),
                                Filters.eq( "category", ANDROID_GOOGLE_PLAY),
                                Filters.eq("_id", applicationConfigurationNameOrId)
                        ));
                    }
                    return qry.first();
                });

        if (mongoGooglePlayApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoGooglePlayApplicationProfile.getObjectId());
        }

        getObjectIndex().index(mongoGooglePlayApplicationProfile);

    }

    public void validate(final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {

        if (googlePlayApplicationConfiguration == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (googlePlayApplicationConfiguration.getCategory()) {
            case ANDROID_GOOGLE_PLAY:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + googlePlayApplicationConfiguration.getCategory());
        }

        getValidationHelper().validateModel(googlePlayApplicationConfiguration);

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
