package dev.getelements.elements.dao.mongo.application;

import dev.getelements.elements.dao.GoogleSignInApplicationConfigurationDao;
import dev.getelements.elements.dao.GoogleSignInApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.application.MongoGoogleSignInApplicationConfiguration;
import dev.getelements.elements.dao.mongo.model.application.MongoProductBundle;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.application.GoogleSignInApplicationConfiguration;
import dev.getelements.elements.model.application.GoogleSignInApplicationConfiguration;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.model.application.ConfigurationCategory.GOOGLE_SIGN_IN;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoGoogleSignInApplicationConfigurationDao implements GoogleSignInApplicationConfigurationDao {

    private Datastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public GoogleSignInApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final GoogleSignInApplicationConfiguration googleSignInApplicationConfiguration) {


        validate(googleSignInApplicationConfiguration);

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoGoogleSignInApplicationConfiguration.class);

        query.filter(
                and(
                        eq("active", false),
                        eq("parent", mongoApplication),
                        eq( "category", GOOGLE_SIGN_IN),
                        eq("uniqueIdentifier", googleSignInApplicationConfiguration.getApplicationId())
                )
        );

        List<MongoProductBundle> mongoProductBundles = null;

        if (googleSignInApplicationConfiguration.getProductBundles() != null &&
                googleSignInApplicationConfiguration.getProductBundles().size() > 0) {
            mongoProductBundles = googleSignInApplicationConfiguration
                    .getProductBundles()
                    .stream()
                    .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                    .collect(Collectors.toList());
        }

        final var builder = new UpdateBuilder();

        if(mongoProductBundles != null){
            builder.with(
                    set("uniqueIdentifier", googleSignInApplicationConfiguration.getApplicationId().trim()),
                    set("active", true),
                    set( "category", googleSignInApplicationConfiguration.getCategory()),
                    set("parent", mongoApplication),
                    set("productBundles", mongoProductBundles)
            );
        } else{
            builder.with(
                    set("uniqueIdentifier", googleSignInApplicationConfiguration.getApplicationId().trim()),
                    set("active", true),
                    set( "category", googleSignInApplicationConfiguration.getCategory()),
                    set("parent", mongoApplication)
            );
        }

        final var mongoGoogleSignInApplicationProfile = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return getBeanMapper().map(mongoGoogleSignInApplicationProfile, GoogleSignInApplicationConfiguration.class);

    }

    @Override
    public GoogleSignInApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoGoogleSignInApplicationConfiguration> query;
        query = getDatastore().find(MongoGoogleSignInApplicationConfiguration.class);

        query.filter(
                and(
                        eq("active", true),
                        eq("parent", mongoApplication),
                        eq( "category", GOOGLE_SIGN_IN)
                )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoGoogleSignInApplicationConfiguration mongoIosApplicationProfile = query.first();

        if (mongoIosApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoIosApplicationProfile, GoogleSignInApplicationConfiguration.class);

    }

    @Override
    public GoogleSignInApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final GoogleSignInApplicationConfiguration googleSignInApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(googleSignInApplicationConfiguration);

        final Query<MongoGoogleSignInApplicationConfiguration> query;
        query = getDatastore().find(MongoGoogleSignInApplicationConfiguration.class);

        query.filter(
                and(
                        eq("active", true),
                        eq("parent", mongoApplication),
                        eq( "category" ,GOOGLE_SIGN_IN)
                )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        List<MongoProductBundle> mongoProductBundles = null;

        if (googleSignInApplicationConfiguration.getProductBundles() != null &&
                googleSignInApplicationConfiguration.getProductBundles().size() > 0) {
            mongoProductBundles = googleSignInApplicationConfiguration
                    .getProductBundles()
                    .stream()
                    .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                    .collect(Collectors.toList());
        }

        final var builder = new UpdateBuilder();

        if(mongoProductBundles != null) {
            builder.with(
                    set("uniqueIdentifier", googleSignInApplicationConfiguration.getApplicationId().trim()),
                    set( "category", googleSignInApplicationConfiguration.getCategory()),
                    set("parent", mongoApplication),
                    set("productBundles", mongoProductBundles),
                    unset("jsonKey")
            );
        } else {
            builder.with(
                    set("uniqueIdentifier", googleSignInApplicationConfiguration.getApplicationId().trim()),
                    set( "category", googleSignInApplicationConfiguration.getCategory()),
                    set("parent", mongoApplication)
            );
        }

        final var mongoGoogleSignInApplicationProfile = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoGoogleSignInApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + applicationConfigurationNameOrId);
        }

        return getBeanMapper().map(mongoGoogleSignInApplicationProfile, GoogleSignInApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoGoogleSignInApplicationConfiguration.class);

        query.filter(
                and(
                        eq("active", true),
                        eq("parent", mongoApplication),
                        eq( "category", GOOGLE_SIGN_IN)
                ));

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoGoogleSignInApplicationProfile = getMongoDBUtils().perform(ds ->
                query.modify(
                        set("active", false)
                ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoGoogleSignInApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoGoogleSignInApplicationProfile.getObjectId());
        }

    }

    public void validate(final GoogleSignInApplicationConfiguration googleSignInApplicationConfiguration) {

        if (googleSignInApplicationConfiguration == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (googleSignInApplicationConfiguration.getCategory()) {
            case GOOGLE_SIGN_IN:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + googleSignInApplicationConfiguration.getCategory());
        }

        getValidationHelper().validateModel(googleSignInApplicationConfiguration);

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
