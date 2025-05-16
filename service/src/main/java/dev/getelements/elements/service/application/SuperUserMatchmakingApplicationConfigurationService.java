package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.MatchmakingApplicationConfigurationService;
import jakarta.inject.Inject;

public class SuperUserMatchmakingApplicationConfigurationService implements MatchmakingApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        getApplicationConfigurationDao().deleteApplicationConfiguration(
                MatchmakingApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId);
    }

    @Override
    public MatchmakingApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        return getApplicationConfigurationDao().getApplicationConfiguration(
                MatchmakingApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId
        );
    }

    @Override
    public MatchmakingApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        return getApplicationConfigurationDao().createApplicationConfiguration(
                applicationNameOrId,
                matchmakingApplicationConfiguration
        );
    }

    @Override
    public MatchmakingApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        return getApplicationConfigurationDao().updateApplicationConfiguration(
                applicationNameOrId,
                matchmakingApplicationConfiguration);
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
