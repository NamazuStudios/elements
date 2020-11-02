package com.namazustudios.socialengine.dao.mongo.application;

import com.mongodb.client.result.UpdateResult;
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
        query = getDatastore().find(MongoMatchmakingApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", false),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", MATCHMAKING),
                Filters.eq("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme())
        ));

        UpdateResult updateResult = query.update(UpdateOperators.set("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme().trim()),
        UpdateOperators.set("category", MATCHMAKING),
        UpdateOperators.set("active", true),
        UpdateOperators.set("success", getBeanMapper().map(matchmakingApplicationConfiguration.getSuccess(), MongoCallbackDefinition.class)),
        UpdateOperators.set("parent", mongoApplication),
        UpdateOperators.set("algorithm", matchmakingApplicationConfiguration.getAlgorithm())
        ).execute(new UpdateOptions().upsert(true));

        final MongoMatchmakingApplicationConfiguration mongoMatchmakingApplicationProfile;

        mongoMatchmakingApplicationProfile = getMongoDBUtils()
            .perform(ds -> {
                if(updateResult.getUpsertedId() != null) {
                    return ds.find(MongoMatchmakingApplicationConfiguration.class)
                            .filter(Filters.eq("_id", updateResult.getUpsertedId())).first();
                } else {
                    return ds.find(MongoMatchmakingApplicationConfiguration.class)
                            .filter(Filters.and(
                                    Filters.eq("active", true),
                                    Filters.eq("parent", mongoApplication),
                                    Filters.eq("category", MATCHMAKING),
                                    Filters.eq("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme())
                            )).first();
                }
            });

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
        query = getDatastore().find(MongoMatchmakingApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", MATCHMAKING)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier = ", applicationConfigurationNameOrId));
        }

        final MongoMatchmakingApplicationConfiguration mongoMatchmakingApplicationProfile = query.first();

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
        query = getDatastore().find(MongoMatchmakingApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", MATCHMAKING)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationProfileNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationProfileNameOrId));
        }

        final UpdateOperations<MongoMatchmakingApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoMatchmakingApplicationConfiguration.class);

        query.update(UpdateOperators.set("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme().trim()),
                UpdateOperators.set("success", getBeanMapper().map(matchmakingApplicationConfiguration.getSuccess(), MongoCallbackDefinition.class)),
                UpdateOperators.set("parent", mongoApplication),
                UpdateOperators.set("category", MATCHMAKING),
                UpdateOperators.set("algorithm", matchmakingApplicationConfiguration.getAlgorithm())
        ).execute(new UpdateOptions().upsert(false));

        final MongoMatchmakingApplicationConfiguration mongoMatchmakingApplicationConfiguration;
        mongoMatchmakingApplicationConfiguration = getMongoDBUtils()
                .perform(ds -> {
                    try {
                        return ds.find(MongoMatchmakingApplicationConfiguration.class)
                                .filter(Filters.and(
                                        Filters.eq("active", true),
                                        Filters.eq("parent", mongoApplication),
                                        Filters.eq("category", MATCHMAKING),
                                        Filters.eq("_id", new ObjectId(applicationProfileNameOrId))
                                )).first();
                    } catch (IllegalArgumentException ex) {
                        return ds.find(MongoMatchmakingApplicationConfiguration.class)
                                .filter(Filters.and(
                                        Filters.eq("active", true),
                                        Filters.eq("parent", mongoApplication),
                                        Filters.eq("category", MATCHMAKING),
                                        Filters.eq("uniqueIdentifier", applicationProfileNameOrId)
                                )).first();
                    }
                });

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
        query = getDatastore().find(MongoMatchmakingApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", MATCHMAKING)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        query.update(UpdateOperators.set("active", false)).execute(new UpdateOptions().upsert(false));

        final MongoMatchmakingApplicationConfiguration mongoMatchmakingApplicationProfile;

        mongoMatchmakingApplicationProfile = getMongoDBUtils()
                .perform(ds -> {
                    try {
                        return ds.find(MongoMatchmakingApplicationConfiguration.class)
                                .filter(Filters.and(
                                        Filters.eq("active", false),
                                        Filters.eq("parent", mongoApplication),
                                        Filters.eq("category", MATCHMAKING),
                                        Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)))
                                ).first();
                    } catch (IllegalArgumentException ex) {
                        return ds.find(MongoMatchmakingApplicationConfiguration.class)
                                .filter(Filters.and(
                                        Filters.eq("active", false),
                                        Filters.eq("parent", mongoApplication),
                                        Filters.eq("category", MATCHMAKING),
                                        Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId))
                                ).first();
                    }
                });

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
