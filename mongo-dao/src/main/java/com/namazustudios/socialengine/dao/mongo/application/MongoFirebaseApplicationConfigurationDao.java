package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoFirebaseApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.application.FirebaseApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.model.application.ConfigurationCategory;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FIREBASE;
import static dev.morphia.query.experimental.filters.Filters.and;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoFirebaseApplicationConfigurationDao extends MongoApplicationConfigurationDao
        implements FirebaseApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private Datastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public FirebaseApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(firebaseApplicationConfiguration);

        final var query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", false),
                eq("parent", mongoApplication),
                eq("category", FIREBASE),
                eq("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim())
            )
        );

        final var mongoFirebaseApplicationProfile = getMongoDBUtils().perform(ds ->
            query.modify(
                set("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim()),
                set("active", true),
                set("category", firebaseApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("serviceAccountCredentials", firebaseApplicationConfiguration.getServiceAccountCredentials().trim())
            ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoFirebaseApplicationProfile);
        return getBeanMapper().map(mongoFirebaseApplicationProfile, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration getApplicationConfiguration(final String applicationConfigurationNameOrId) {

        final var query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("category", FIREBASE)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoFirebaseApplicationConfiguration = query.first();

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new FirebaseApplicationConfigurationNotFoundException("application configuration " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category", FIREBASE)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoFirebaseApplicationConfiguration = query.first();

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new FirebaseApplicationConfigurationNotFoundException("application configuration " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(firebaseApplicationConfiguration);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category", FIREBASE)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoFirebaseApplicationConfiguration = getMongoDBUtils().perform(ds ->
            query.modify(
                set("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim()),
                set("category", firebaseApplicationConfiguration.getCategory()),
                set("parent", mongoApplication),
                set("serviceAccountCredentials", firebaseApplicationConfiguration.getServiceAccountCredentials().trim())
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new FirebaseApplicationConfigurationNotFoundException("application configuration " + applicationConfigurationNameOrId + " not found.");
        }

        getObjectIndex().index(mongoFirebaseApplicationConfiguration);
        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq( "category", FIREBASE)
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoFirebaseApplicationProfile = getMongoDBUtils().perform(ds ->
            query.modify(
                set("active", false)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoFirebaseApplicationProfile == null) {
            throw new FirebaseApplicationConfigurationNotFoundException("profile with ID not found: " + applicationConfigurationNameOrId);
        }

        getObjectIndex().index(mongoFirebaseApplicationProfile);

    }

    public void validate(final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        if (firebaseApplicationConfiguration == null) {
            throw new InvalidDataException("firebaseApplicationConfiguration must not be null.");
        }

        if (firebaseApplicationConfiguration.getCategory() == null) {
            firebaseApplicationConfiguration.setCategory(ConfigurationCategory.FIREBASE);
        }

        switch (firebaseApplicationConfiguration.getCategory()) {
            case FIREBASE:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + firebaseApplicationConfiguration.getCategory());
        }

        getValidationHelper().validateModel(firebaseApplicationConfiguration);

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
