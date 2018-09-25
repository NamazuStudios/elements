package com.namazustudios.socialengine.service.gameon.client.invoker.builder;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnAdminPrizeInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.v1.V1StandardAdminGameOnPrizeInvoker;

import javax.ws.rs.client.Client;

public class DefaultGameOnPrizeInvokerBuilder extends AbstractAdminRequestBuilder<GameOnAdminPrizeInvoker>
                                              implements GameOnAdminPrizeInvoker.Builder {

    @Override
    protected GameOnAdminPrizeInvoker doBuild(final Client client,
                                              final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        final String adminApiKey = gameOnApplicationConfiguration.getAdminApiKey();
        return new V1StandardAdminGameOnPrizeInvoker(client, adminApiKey);
    }

}
