package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.MatchmakingApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.MongoCallbackDefinition;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoMatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.MATCHMAKING;
import static dev.morphia.query.experimental.filters.Filters.and;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoMatchmakingApplicationConfigurationDao implements MatchmakingApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private Datastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public MatchmakingApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {

        validate(matchmakingApplicationConfiguration);

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoMatchmakingApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", false),
                eq("parent", mongoApplication),
                eq("category", MATCHMAKING),
                eq("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme())
            )
        );

        final var mongoMatchmakingApplicationConfiguration = getMongoDBUtils().perform(ds ->
            query.modify(
                set("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme().trim()),
                set("category", MATCHMAKING),
                set("active", true),
                set("success", getBeanMapper().map(matchmakingApplicationConfiguration.getSuccess(), MongoCallbackDefinition.class)),
                set("parent", mongoApplication),
                set("algorithm", matchmakingApplicationConfiguration.getAlgorithm())
            ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoMatchmakingApplicationConfiguration);
        return getBeanMapper().map(mongoMatchmakingApplicationConfiguration, MatchmakingApplicationConfiguration.class);

    }

    @Override
    public MatchmakingApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoMatchmakingApplicationConfiguration> query;
        query = getDatastore().find(MongoMatchmakingApplicationConfiguration.class);

        query.filter(and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq("category", MATCHMAKING)
        ));

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier = ", applicationConfigurationNameOrId));
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

        validate(matchmakingApplicationConfiguration);

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoMatchmakingApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq("category", MATCHMAKING)
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationProfileNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationProfileNameOrId));
        }

        final var mongoMatchmakingApplicationConfiguration = getMongoDBUtils().perform(ds ->
            query.modify(
                set("uniqueIdentifier", matchmakingApplicationConfiguration.getScheme().trim()),
                set("success", getBeanMapper().map(matchmakingApplicationConfiguration.getSuccess(), MongoCallbackDefinition.class)),
                set("parent", mongoApplication),
                set("category", MATCHMAKING),
                set("algorithm", matchmakingApplicationConfiguration.getAlgorithm())
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

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

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoMatchmakingApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq("category", MATCHMAKING)
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoMatchmakingApplicationConfiguration = getMongoDBUtils().perform( ds ->
            query.modify(
                set("active", false)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoMatchmakingApplicationConfiguration == null) {
            throw new NotFoundException("configuration with ID not found: " + applicationConfigurationNameOrId);
        }

        getObjectIndex().index(mongoMatchmakingApplicationConfiguration);

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
