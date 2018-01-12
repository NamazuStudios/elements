package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.MatchmakingApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;

public class MongoMatchmakingApplicationConfigurationDao implements MatchmakingApplicationConfigurationDao {

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationConfigurationNameOrId) {

    }

    @Override
    public MatchmakingApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        return null;
    }

    @Override
    public MatchmakingApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        return null;
    }

    @Override
    public MatchmakingApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId, String applicationConfigurationNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        return null;
    }

}
