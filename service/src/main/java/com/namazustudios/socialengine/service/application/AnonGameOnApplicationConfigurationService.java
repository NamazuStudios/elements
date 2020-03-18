package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.security.InsufficientPermissionException;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.service.GameOnApplicationConfigurationService;

import javax.inject.Inject;

public class AnonGameOnApplicationConfigurationService implements GameOnApplicationConfigurationService {

    private GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        throw new InsufficientPermissionException();
    }

    @Override
    public GameOnApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        final GameOnApplicationConfiguration gameOnApplicationConfiguration;
        gameOnApplicationConfiguration = getGameOnApplicationConfigurationDao().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
        gameOnApplicationConfiguration.setAdminApiKey(null);
        return gameOnApplicationConfiguration;
    }

    @Override
    public GameOnApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        throw new InsufficientPermissionException();
    }

    @Override
    public GameOnApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        throw new InsufficientPermissionException();
    }

    public GameOnApplicationConfigurationDao getGameOnApplicationConfigurationDao() {
        return gameOnApplicationConfigurationDao;
    }

    @Inject
    public void setGameOnApplicationConfigurationDao(GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao) {
        this.gameOnApplicationConfigurationDao = gameOnApplicationConfigurationDao;
    }

}
