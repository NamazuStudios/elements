package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoGameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.application.ConfigurationCategory;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.util.List;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.AMAZON_GAME_ON;

public class MongoGameOnApplicationConfigurationDao implements GameOnApplicationConfigurationDao {

    private MongoApplicationConfigurationOperations mongoApplicationConfigurationOperations;

    @Override
    public GameOnApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        return getMongoApplicationConfigurationOperations().getApplicationConfiguration(
                GameOnApplicationConfiguration.class,
                MongoGameOnApplicationConfiguration.class,
                AMAZON_GAME_ON,
                applicationNameOrId,
                applicationConfigurationNameOrId);
    }

    @Override
    public List<GameOnApplicationConfiguration> getConfigurationsForApplication(String applicationNameOrId) {
        return getMongoApplicationConfigurationOperations().getApplicationConfigurationsForApplication(
            GameOnApplicationConfiguration.class,
            MongoGameOnApplicationConfiguration.class,
            AMAZON_GAME_ON,
            applicationNameOrId);
    }

    @Override
    public GameOnApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        return getMongoApplicationConfigurationOperations().createOrUpdateInactiveApplicationConfiguration(
            GameOnApplicationConfiguration.class,
            MongoGameOnApplicationConfiguration.class,
            this::validate,
            o -> update(o, gameOnApplicationConfiguration),
            applicationNameOrId,
            gameOnApplicationConfiguration);
    }

    @Override
    public GameOnApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        return getMongoApplicationConfigurationOperations().updateApplicationConfiguration(
            GameOnApplicationConfiguration.class,
            MongoGameOnApplicationConfiguration.class,
            this::validate,
            o -> update(o, gameOnApplicationConfiguration),
            applicationNameOrId,
            applicationConfigurationNameOrId,
            gameOnApplicationConfiguration);
    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        getMongoApplicationConfigurationOperations().softDeleteApplicationConfiguration(
            MongoGameOnApplicationConfiguration.class,
            AMAZON_GAME_ON,
            applicationNameOrId,
            applicationConfigurationNameOrId);
    }

    public MongoApplicationConfigurationOperations getMongoApplicationConfigurationOperations() {
        return mongoApplicationConfigurationOperations;
    }

    @Inject
    public void setMongoApplicationConfigurationOperations(MongoApplicationConfigurationOperations mongoApplicationConfigurationOperations) {
        this.mongoApplicationConfigurationOperations = mongoApplicationConfigurationOperations;
    }

    protected void validate(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        gameOnApplicationConfiguration.setCategory(AMAZON_GAME_ON);
        gameOnApplicationConfiguration.setUniqueIdentifier(gameOnApplicationConfiguration.getGameId());
    }

    protected void update(final UpdateOperations<MongoGameOnApplicationConfiguration> operations,
                          final GameOnApplicationConfiguration gameOnApplicationConfiguration) {

        operations.set("publicApiKey", gameOnApplicationConfiguration.getPublicApiKey());
        operations.set("adminApiKey", gameOnApplicationConfiguration.getAdminApiKey());

        if (gameOnApplicationConfiguration.getPublicKey() == null) {
            operations.unset("publicKey");
        } else {
            operations.set("publicKey", gameOnApplicationConfiguration.getPublicKey());
        }

    }

}
