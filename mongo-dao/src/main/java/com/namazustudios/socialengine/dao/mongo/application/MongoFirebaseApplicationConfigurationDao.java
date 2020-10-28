package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoFirebaseApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.ConfigurationCategory;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FIREBASE;

public class MongoFirebaseApplicationConfigurationDao extends MongoApplicationConfigurationDao
        implements FirebaseApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public FirebaseApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(firebaseApplicationConfiguration);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFirebaseApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("category").equal(FIREBASE),
            query.criteria("uniqueIdentifier").equal(firebaseApplicationConfiguration.getProjectId().trim())
        );

        final UpdateOperations<MongoFirebaseApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoFirebaseApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim());
        updateOperations.set("active", true);
        updateOperations.set("category", firebaseApplicationConfiguration.getCategory());
        updateOperations.set("parent", mongoApplication);
        updateOperations.set("serviceAccountCredentials", firebaseApplicationConfiguration.getServiceAccountCredentials().trim());

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true);

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationProfile;
        mongoFirebaseApplicationProfile = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        getObjectIndex().index(mongoFirebaseApplicationProfile);
        return getBeanMapper().map(mongoFirebaseApplicationProfile, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration getApplicationConfiguration(final String applicationConfigurationNameOrId) {

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFirebaseApplicationConfiguration.class);

        query.and(
                query.criteria("active").equal(true),
                query.criteria( "category").equal(FIREBASE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationConfiguration = query.get();

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFirebaseApplicationConfiguration.class);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("parent").equal(mongoApplication),
                query.criteria( "category").equal(FIREBASE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationConfiguration = query.get();

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(firebaseApplicationConfiguration);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFirebaseApplicationConfiguration.class);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("parent").equal(mongoApplication),
                query.criteria( "category").equal(FIREBASE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoFirebaseApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoFirebaseApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim());
        updateOperations.set("category", firebaseApplicationConfiguration.getCategory());
        updateOperations.set("parent", mongoApplication);
        updateOperations.set("serviceAccountCredentials", firebaseApplicationConfiguration.getServiceAccountCredentials().trim());

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationConfiguration;
        mongoFirebaseApplicationConfiguration = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoFirebaseApplicationConfiguration);
        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoFirebaseApplicationConfiguration.class);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("parent").equal(mongoApplication),
                query.criteria( "category").equal(FIREBASE)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final UpdateOperations<MongoFirebaseApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoFirebaseApplicationConfiguration.class);

        updateOperations.set("active", false);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationProfile;

        mongoFirebaseApplicationProfile = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoFirebaseApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoFirebaseApplicationProfile.getObjectId());
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
