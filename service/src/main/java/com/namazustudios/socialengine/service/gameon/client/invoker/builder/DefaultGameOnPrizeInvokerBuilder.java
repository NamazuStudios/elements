package com.namazustudios.socialengine.service.gameon.client.invoker.builder;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnPrizeInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.v1.V1StandardGameOnPrizeInvoker;

import javax.ws.rs.client.Client;

public class DefaultGameOnPrizeInvokerBuilder extends AbstractAdminRequestBuilder<GameOnPrizeInvoker>
                                              implements GameOnPrizeInvoker.Builder {

    @Override
    protected GameOnPrizeInvoker doBuild(final Client client,
                                         final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        final String adminApiKey = gameOnApplicationConfiguration.getAdminApiKey();
        return new V1StandardGameOnPrizeInvoker(client, adminApiKey);
    }

}
