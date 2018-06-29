package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.service.GameOnApplicationConfigurationService;

import javax.inject.Inject;

public class SuperUserGameOnApplicationConfigurationService implements GameOnApplicationConfigurationService {

    private GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        getGameOnApplicationConfigurationDao()
                .softDeleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public GameOnApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        return getGameOnApplicationConfigurationDao()
                .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public GameOnApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        return getGameOnApplicationConfigurationDao()
                .createOrUpdateInactiveApplicationConfiguration(applicationNameOrId, gameOnApplicationConfiguration);
    }

    @Override
    public GameOnApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        return getGameOnApplicationConfigurationDao()
                .updateApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId, gameOnApplicationConfiguration);
    }

    public GameOnApplicationConfigurationDao getGameOnApplicationConfigurationDao() {
        return gameOnApplicationConfigurationDao;
    }

    @Inject
    public void setGameOnApplicationConfigurationDao(GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao) {
        this.gameOnApplicationConfigurationDao = gameOnApplicationConfigurationDao;
    }

}

