package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;

public interface GameOnApplicationConfigurationDao {

    GameOnApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

    GameOnApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(String applicationNameOrId, GameOnApplicationConfiguration gameOnApplicationConfiguration);

    GameOnApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, GameOnApplicationConfiguration gameOnApplicationConfiguration);

    void softDeleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}

