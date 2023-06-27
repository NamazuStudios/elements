package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.MatchmakingApplicationConfigurationDao;
import dev.getelements.elements.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.service.MatchmakingApplicationConfigurationService;

import javax.inject.Inject;

public class SuperUserMatchmakingApplicationConfigurationService implements MatchmakingApplicationConfigurationService {

    private MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        getMatchmakingApplicationConfigurationDao().softDeleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public MatchmakingApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        return getMatchmakingApplicationConfigurationDao().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public MatchmakingApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        return getMatchmakingApplicationConfigurationDao().createOrUpdateInactiveApplicationConfiguration(applicationNameOrId, matchmakingApplicationConfiguration);
    }

    @Override
    public MatchmakingApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        return getMatchmakingApplicationConfigurationDao().updateApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId, matchmakingApplicationConfiguration);
    }

    public MatchmakingApplicationConfigurationDao getMatchmakingApplicationConfigurationDao() {
        return matchmakingApplicationConfigurationDao;
    }

    @Inject
    public void setMatchmakingApplicationConfigurationDao(MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao) {
        this.matchmakingApplicationConfigurationDao = matchmakingApplicationConfigurationDao;
    }

}
