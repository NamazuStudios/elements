package dev.getelements.elements.dao.mongo.application;

import com.namazustudios.elements.fts.ObjectIndex;
import dev.getelements.elements.dao.FacebookApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.application.MongoFacebookApplicationConfiguration;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.application.ConfigurationCategory;
import dev.getelements.elements.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.model.application.ConfigurationCategory.FACEBOOK;
import static dev.morphia.query.experimental.filters.Filters.and;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

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

        validate(facebookApplicationConfiguration);

        final var query = getDatastore().find(MongoFacebookApplicationConfiguration.class);
        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        query.filter(
            and(
                eq("active", false),
                eq("parent", mongoApplication),
                eq("category", FACEBOOK),
                eq("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId())
            )
        );

        final var mongoFacebookApplicationProfile = query.modify(
            set("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId().trim()),
            set("active", true),
            set("category", facebookApplicationConfiguration.getCategory()),
            set("parent", mongoApplication),
            set("applicationSecret", facebookApplicationConfiguration.getApplicationSecret().trim())
        ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER));

        getObjectIndex().index(mongoFacebookApplicationProfile);
        return getBeanMapper().map(mongoFacebookApplicationProfile, FacebookApplicationConfiguration.class);

    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(final String applicationConfigurationNameOrId) {

        final var query = getDatastore().find(MongoFacebookApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("category", FACEBOOK)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
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

        final var query = getDatastore().find(MongoFacebookApplicationConfiguration.class);
        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        query.filter(
            and(
               eq("active", true),
               eq("parent", mongoApplication),
               eq("category", FACEBOOK)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoFacebookApplicationConfiguration = query.first();

        if (mongoFacebookApplicationConfiguration == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFacebookApplicationConfiguration, FacebookApplicationConfiguration.class);

    }

    @Override
    public FacebookApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(facebookApplicationConfiguration);

        final var query = getDatastore().find(MongoFacebookApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq("category", FACEBOOK)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoFacebookApplicationConfiguration = query.modify(
            set("uniqueIdentifier", facebookApplicationConfiguration.getApplicationId().trim()),
            set("category", facebookApplicationConfiguration.getCategory()),
            set("parent", mongoApplication),
            set("applicationSecret", facebookApplicationConfiguration.getApplicationSecret().trim())
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (mongoFacebookApplicationConfiguration == null) {
            throw new NotFoundException("profile with ID not found: " + applicationConfigurationNameOrId);
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

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq("category", FACEBOOK)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoFacebookApplicationProfile = getMongoDBUtils().perform(ds ->
            query.modify(
                set("active", false)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

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
