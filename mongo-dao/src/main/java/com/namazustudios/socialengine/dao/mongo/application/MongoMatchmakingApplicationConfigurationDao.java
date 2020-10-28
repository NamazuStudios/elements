package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.socialengine.dao.MatchmakingApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoCallbackDefinition;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoMatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.*;

public class MongoMatchmakingApplicationConfigurationDao implements MatchmakingApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public MatchmakingApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(matchmakingApplicationConfiguration);

        final Query<MongoMatchmakingApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoMatchmakingApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("category").equal(MATCHMAKING),
            query.criteria("uniqueIdentifier").equal(matchmakingApplicationConfiguration.getScheme())
        );

        final UpdateOperations<MongoMatchmakingApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoMatchmakingApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme().trim());
        updateOperations.set("category", MATCHMAKING);
        updateOperations.set("active", true);
        updateOperations.set("success", getBeanMapper().map(matchmakingApplicationConfiguration.getSuccess(), MongoCallbackDefinition.class));
        updateOperations.set("parent", mongoApplication);
        updateOperations.set("algorithm", matchmakingApplicationConfiguration.getAlgorithm());

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(true);

        final MongoMatchmakingApplicationConfiguration mongoMatchmakingApplicationProfile;

        mongoMatchmakingApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        getObjectIndex().index(mongoMatchmakingApplicationProfile);
        return getBeanMapper().map(mongoMatchmakingApplicationProfile, MatchmakingApplicationConfiguration.class);

    }

    @Override
    public MatchmakingApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoMatchmakingApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoMatchmakingApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("category").equal(MATCHMAKING)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final MongoMatchmakingApplicationConfiguration mongoMatchmakingApplicationProfile = query.get();

        if (mongoMatchmakingApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoMatchmakingApplicationProfile, MatchmakingApplicationConfiguration.class);

    }

    @Override
    public MatchmakingApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(matchmakingApplicationConfiguration);

        final Query<MongoMatchmakingApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoMatchmakingApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("category").equal(MATCHMAKING)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoMatchmakingApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoMatchmakingApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme().trim());
        updateOperations.set("success", getBeanMapper().map(matchmakingApplicationConfiguration.getSuccess(), MongoCallbackDefinition.class));
        updateOperations.set("parent", mongoApplication);
        updateOperations.set("category", MATCHMAKING);
        updateOperations.set("algorithm", matchmakingApplicationConfiguration.getAlgorithm());

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoMatchmakingApplicationConfiguration mongoMatchmakingApplicationConfiguration;
        mongoMatchmakingApplicationConfiguration = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoMatchmakingApplicationConfiguration == null) {
            throw new NotFoundException("configuration with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoMatchmakingApplicationConfiguration);
        return getBeanMapper().map(matchmakingApplicationConfiguration, MatchmakingApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoMatchmakingApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoMatchmakingApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("category").equal(MATCHMAKING)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final UpdateOperations<MongoMatchmakingApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoMatchmakingApplicationConfiguration.class);

        updateOperations.set("active", false);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoMatchmakingApplicationConfiguration mongoMatchmakingApplicationProfile;

        mongoMatchmakingApplicationProfile = getMongoDBUtils()
                .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoMatchmakingApplicationProfile == null) {
            throw new NotFoundException("configuration with ID not found: " + applicationConfigurationNameOrId);
        }

        getObjectIndex().index(mongoMatchmakingApplicationProfile);

    }

    public void validate(final MatchmakingApplicationConfiguration configuration) {

        if (configuration == null) {
            throw new InvalidDataException("configuration must not be null.");
        }

        getValidationHelper().validateModel(configuration);

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
