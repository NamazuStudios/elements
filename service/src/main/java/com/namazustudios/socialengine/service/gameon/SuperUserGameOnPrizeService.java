package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import com.namazustudios.socialengine.service.GameOnAdminPrizeService;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnPrizeInvoker;

import javax.inject.Inject;
import javax.inject.Provider;

public class SuperUserGameOnPrizeService implements GameOnAdminPrizeService {

    private GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao;

    private Provider<GameOnPrizeInvoker.Builder> gameOnPrizeInvokerBuilderProvider;

    @Override
    public GameOnGetPrizeListResponse getPrizes(final String applicationId, final String configurationId) {

        final GameOnApplicationConfiguration gameOnApplicationConfiguration =
            getGameOnApplicationConfigurationDao().
            getApplicationConfiguration(applicationId, configurationId);

        return getGameOnPrizeInvokerBuilderProvider()
            .get()
            .withConfiguration(gameOnApplicationConfiguration)
            .build()
            .getPrizes();

    }

    @Override
    public GameOnAddPrizeListResponse addPrizes(final String applicationId, final String configurationId,
                                                final GameOnAddPrizeListRequest addPrizeListRequest) {

        final GameOnApplicationConfiguration gameOnApplicationConfiguration =
            getGameOnApplicationConfigurationDao().
            getApplicationConfiguration(applicationId, configurationId);

        return getGameOnPrizeInvokerBuilderProvider()
            .get()
            .withConfiguration(gameOnApplicationConfiguration)
            .build()
            .addPrizes(addPrizeListRequest);

    }

    public GameOnApplicationConfigurationDao getGameOnApplicationConfigurationDao() {
        return gameOnApplicationConfigurationDao;
    }

    @Inject
    public void setGameOnApplicationConfigurationDao(GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao) {
        this.gameOnApplicationConfigurationDao = gameOnApplicationConfigurationDao;
    }

    public Provider<GameOnPrizeInvoker.Builder> getGameOnPrizeInvokerBuilderProvider() {
        return gameOnPrizeInvokerBuilderProvider;
    }

    @Inject
    public void setGameOnPrizeInvokerBuilderProvider(Provider<GameOnPrizeInvoker.Builder> gameOnPrizeInvokerBuilderProvider) {
        this.gameOnPrizeInvokerBuilderProvider = gameOnPrizeInvokerBuilderProvider;
    }

}
