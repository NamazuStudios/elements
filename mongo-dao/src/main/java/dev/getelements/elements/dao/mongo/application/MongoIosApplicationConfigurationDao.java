package dev.getelements.elements.dao.mongo.application;

import com.namazustudios.elements.fts.ObjectIndex;
import dev.getelements.elements.dao.IosApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.application.MongoAppleSignInConfiguration;
import dev.getelements.elements.dao.mongo.model.application.MongoIosApplicationConfiguration;
import dev.getelements.elements.dao.mongo.model.application.MongoProductBundle;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.exception.application.ApplicationConfigurationNotFoundException;
import dev.getelements.elements.model.application.IosApplicationConfiguration;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.model.application.ConfigurationCategory.IOS_APP_STORE;
import static dev.morphia.query.experimental.filters.Filters.and;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static dev.morphia.query.experimental.updates.UpdateOperators.unset;
import static java.lang.String.format;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoIosApplicationConfigurationDao implements IosApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private Datastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public IosApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final IosApplicationConfiguration iosApplicationConfiguration) {

        validate(iosApplicationConfiguration);

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoIosApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", false),
                eq("parent", mongoApplication),
                eq("category", IOS_APP_STORE),
                eq("uniqueIdentifier", iosApplicationConfiguration.getApplicationId()
            )
        ));

        final var builder = new UpdateBuilder();

        if (iosApplicationConfiguration.getProductBundles() != null &&
            iosApplicationConfiguration.getProductBundles().size() > 0) {

            final var mongoProductBundles = iosApplicationConfiguration
                .getProductBundles()
                .stream()
                .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                .collect(Collectors.toList());

            builder.with(
                set("uniqueIdentifier", iosApplicationConfiguration.getApplicationId().trim()),
                set("active", true),
                set( "category", iosApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("productBundles", mongoProductBundles)
            );

        } else {
            builder.with(
                set("uniqueIdentifier", iosApplicationConfiguration.getApplicationId().trim()),
                set("active", true),
                set( "category", iosApplicationConfiguration.getCategory()),
                set("parent", mongoApplication)
            );
        }

        final var mongoIosApplicationProfile = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoIosApplicationProfile);

        return getBeanMapper().map(mongoIosApplicationProfile, IosApplicationConfiguration.class);

    }

    @Override
    public IosApplicationConfiguration getIosApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoIosApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category", IOS_APP_STORE)
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoIosApplicationConfiguration mongoIosApplicationProfile = query.first();

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

        validate(iosApplicationConfiguration);

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final var query = getDatastore().find(MongoIosApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category", IOS_APP_STORE)
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationProfileNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier = ", applicationProfileNameOrId));
        }

        final var builder = new UpdateBuilder();

        final var mongoAppleSignInConfiguration = getBeanMapper()
            .map(iosApplicationConfiguration.getAppleSignInConfiguration(), MongoAppleSignInConfiguration.class);

        if (iosApplicationConfiguration.getProductBundles() != null &&
            iosApplicationConfiguration.getProductBundles().size() > 0) {

            final var mongoProductBundles = iosApplicationConfiguration
                .getProductBundles()
                .stream()
                .map(pb -> getBeanMapper().map(pb, MongoProductBundle.class))
                .collect(Collectors.toList());

            builder.with(
                set("uniqueIdentifier", iosApplicationConfiguration.getApplicationId().trim()),
                set("category", iosApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("appleSignInConfiguration", mongoAppleSignInConfiguration),
                set("productBundles", mongoProductBundles)
            );

        } else {
            builder.with(
                set("uniqueIdentifier", iosApplicationConfiguration.getApplicationId().trim()),
                set("category", iosApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("appleSignInConfiguration", mongoAppleSignInConfiguration),
                unset("productBundles")
            );
        }

        final var mongoIosApplicationConfiguration = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoIosApplicationConfiguration == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoIosApplicationConfiguration);
        return getBeanMapper().map(mongoIosApplicationConfiguration, IosApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoIosApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category", IOS_APP_STORE)
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier = ", applicationConfigurationNameOrId));
        }

        final var mongoIosApplicationConfiguration = getMongoDBUtils().perform(ds ->
            query.modify(
                set("active", false)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoIosApplicationConfiguration == null) {
            throw new NotFoundException("profile with ID not found: " + applicationConfigurationNameOrId);
        }

        getObjectIndex().index(mongoIosApplicationConfiguration);

    }

    public void validate(final IosApplicationConfiguration iosApplicationConfiguration) {

        if (iosApplicationConfiguration == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (iosApplicationConfiguration.getCategory()) {
            case IOS_APP_STORE:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + iosApplicationConfiguration.getCategory());
        }

        getValidationHelper().validateModel(iosApplicationConfiguration);

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
