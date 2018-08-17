package com.namazustudios.socialengine.service.gameon.client.invoker.builder;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.service.gameon.client.invoker.AdminRequestBuilder;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

public abstract class AbstractAdminRequestBuilder<BuiltT> implements AdminRequestBuilder<BuiltT> {

    private Client client;

    private GameOnApplicationConfiguration gameOnApplicationConfiguration;

    @Override
    public AdminRequestBuilder<BuiltT> withConfiguration(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        this.gameOnApplicationConfiguration = gameOnApplicationConfiguration;
        return this;
    }

    @Override
    public BuiltT build() {
        if (gameOnApplicationConfiguration == null) throw new IllegalStateException("Configuration not specified.");
        final String adminApiKey = gameOnApplicationConfiguration.getAdminApiKey();
        if (adminApiKey == null) throw new IllegalStateException("Admin API Key Not Specified.");
        return doBuild(getClient(), gameOnApplicationConfiguration);
    }

    protected abstract BuiltT doBuild(final Client client,
                                      final GameOnApplicationConfiguration gameOnApplicationConfiguration);

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
