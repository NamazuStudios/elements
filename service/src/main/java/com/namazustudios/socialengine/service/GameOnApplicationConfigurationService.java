package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;


public interface GameOnApplicationConfigurationService {

    GameOnApplicationConfiguration getApplicationConfiguration(
        String applicationNameOrId,
        String applicationConfigurationNameOrId);

    GameOnApplicationConfiguration createApplicationConfiguration(
        String applicationNameOrId,
        GameOnApplicationConfiguration gameOnApplicationConfiguration);

    GameOnApplicationConfiguration updateApplicationConfiguration(
        String applicationNameOrId,
        String applicationConfigurationNameOrId,
        GameOnApplicationConfiguration gameOnApplicationConfiguration);

    void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}
