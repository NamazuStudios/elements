package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.MatchmakingApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.service.MatchmakingApplicationConfigurationService;

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
