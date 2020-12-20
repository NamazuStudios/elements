package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Used to interact with the GameOn Admin API to manage available prizes.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.scoped.gameon.admin.prize"),
    @ExposedModuleDefinition(value = "namazu.elements.service.unscoped.gameon.admin.prize", annotation = Unscoped.class)
})
public interface GameOnAdminPrizeService {

    /**
     * Lists all available prizes.
     *
     * @param applicationId the applicationId from {@link Application#getId()}
     * @param configurationId the configurationId from {@link GameOnApplicationConfiguration#getId()}
     *
     * @return the {@link GameOnGetPrizeListResponse}, never null
     */
    GameOnGetPrizeListResponse getPrizes(String applicationId, String configurationId);

    /**
     * Adds new prizes to the game using the supplied {@link GameOnAddPrizeListRequest}.
     *
     * @param applicationId the applicationId from {@link Application#getId()}
     * @param configurationId the configurationId from {@link GameOnApplicationConfiguration#getId()}
     *
     * @return the {@link GameOnGetPrizeListResponse}, never null
     */
    GameOnAddPrizeListResponse addPrizes(
        String applicationId, String configurationId,
        GameOnAddPrizeListRequest addPrizeListRequest);

}
