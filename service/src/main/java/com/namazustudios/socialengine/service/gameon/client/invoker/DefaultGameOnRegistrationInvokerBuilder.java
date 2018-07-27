package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.service.gameon.client.invoker.v1.V1StandardSecurityRegistrationInvoker;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

public class DefaultGameOnRegistrationInvokerBuilder implements GameOnRegistrationInvoker.Builder {

    private Client client;

    private GameOnRegistration gameOnRegistration;

    private GameOnApplicationConfiguration gameOnApplicationConfiguration;

    @Override
    public DefaultGameOnRegistrationInvokerBuilder withRegistration(final GameOnRegistration gameOnRegistration) {
        this.gameOnRegistration = gameOnRegistration;
        return this;
    }

    @Override
    public DefaultGameOnRegistrationInvokerBuilder withConfiguration(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        this.gameOnApplicationConfiguration = gameOnApplicationConfiguration;
        return this;
    }

    @Override
    public GameOnRegistrationInvoker build() {

        if (gameOnRegistration == null) throw new IllegalStateException("GameOn Registration unspecified.");
        if (gameOnRegistration.getProfile() == null) throw new IllegalStateException("Profile unspecified.");
        if (gameOnApplicationConfiguration == null) throw new IllegalStateException("GameOn Configuration unspecified.");

        if (gameOnApplicationConfiguration.getPublicKey() == null) {
            return new V1StandardSecurityRegistrationInvoker(getClient(), gameOnRegistration, gameOnApplicationConfiguration);
        } else {
            throw new NotImplementedException("Advanced security is not currently supported.");
        }

    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
