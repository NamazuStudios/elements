package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.GooglePlayApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoGooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoProductBundle;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.ANDROID_GOOGLE_PLAY;
import static dev.morphia.query.experimental.filters.Filters.and;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static dev.morphia.query.experimental.updates.UpdateOperators.unset;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoGooglePlayApplicationConfigurationDao implements GooglePlayApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private Datastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public GooglePlayApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {


        validate(googlePlayApplicationConfiguration);

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoGooglePlayApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", false),
                eq("parent", mongoApplication),
                eq( "category", ANDROID_GOOGLE_PLAY),
                eq("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId())
            )
        );

        List<MongoProductBundle> mongoProductBundles = null;

        if (googlePlayApplicationConfiguration.getProductBundles() != null &&
            googlePlayApplicationConfiguration.getProductBundles().size() > 0) {
            mongoProductBundles = googlePlayApplicationConfiguration
                .getProductBundles()
                .stream()
                .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                .collect(Collectors.toList());
        }

        final var builder = new UpdateBuilder();

        if (googlePlayApplicationConfiguration.getJsonKey() != null && mongoProductBundles != null) {
            builder.with(
                set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                set("active", true),
                set( "category", googlePlayApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("jsonKey", googlePlayApplicationConfiguration.getJsonKey()),
                set("productBundles", mongoProductBundles)
            );
        } else if(googlePlayApplicationConfiguration.getJsonKey() != null) {
            builder.with(
                set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                set("active", true),
                set( "category", googlePlayApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("jsonKey", googlePlayApplicationConfiguration.getJsonKey())
            );
        } else if(mongoProductBundles != null){
            builder.with(
                set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                set("active", true),
                set( "category", googlePlayApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("productBundles", mongoProductBundles)
            );
        } else{
            builder.with(
                set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                set("active", true),
                set( "category", googlePlayApplicationConfiguration.getCategory()),
                set("parent", mongoApplication)
            );
        }

        final var mongoGooglePlayApplicationProfile = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

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

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category", ANDROID_GOOGLE_PLAY)
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
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
            final String applicationConfigurationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(googlePlayApplicationConfiguration);

        final Query<MongoGooglePlayApplicationConfiguration> query;
        query = getDatastore().find(MongoGooglePlayApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category" ,ANDROID_GOOGLE_PLAY)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
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

        final var builder = new UpdateBuilder();

        if (googlePlayApplicationConfiguration.getJsonKey() != null && mongoProductBundles != null) {
            builder.with(
                set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                set( "category", googlePlayApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("jsonKey", googlePlayApplicationConfiguration.getJsonKey()),
                set("productBundles", mongoProductBundles)
            );
        } else if(googlePlayApplicationConfiguration.getJsonKey() != null) {
            builder.with(
                set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                set( "category", googlePlayApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("jsonKey", googlePlayApplicationConfiguration.getJsonKey()),
                unset("productBundles")
            );
        } else if(mongoProductBundles != null) {
            builder.with(
                set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                set( "category", googlePlayApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("productBundles", mongoProductBundles),
                unset("jsonKey")
            );
        } else {
            builder.with(
                set("uniqueIdentifier", googlePlayApplicationConfiguration.getApplicationId().trim()),
                set( "category", googlePlayApplicationConfiguration.getCategory()),
                set("parent", mongoApplication)
            );
        }

        final var mongoGooglePlayApplicationProfile = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoGooglePlayApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + applicationConfigurationNameOrId);
        }

        getObjectIndex().index(mongoGooglePlayApplicationProfile);
        return getBeanMapper().map(mongoGooglePlayApplicationProfile, GooglePlayApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoGooglePlayApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category", ANDROID_GOOGLE_PLAY)
        ));

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        query.update(set("active", false)).execute(new UpdateOptions().upsert(false));

        final var mongoGooglePlayApplicationProfile = getMongoDBUtils().perform(ds ->
            query.modify(
                set("active", false)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

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
