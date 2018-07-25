package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.model.gameon.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnAuthenticationInvoker.Builder;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

public class DefaultGameOnAuthenticationInvokerBuilder implements Builder {

    private Client client;

    private GameOnSession gameOnSession;

    private GameOnRegistration gameOnRegistration;

    private GameOnApplicationConfiguration gameOnApplicationConfiguration;

    @Override
    public Builder withSession(final GameOnSession gameOnSession) {
        this.gameOnSession = gameOnSession;
        return this;
    }

    @Override
    public Builder withRegistration(final GameOnRegistration gameOnRegistration) {
        this.gameOnRegistration = gameOnRegistration;
        return this;
    }

    @Override
    public Builder withConfiguration(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        this.gameOnApplicationConfiguration = gameOnApplicationConfiguration;
        return this;
    }

    @Override
    public GameOnAuthenticationInvoker build() {

        if (gameOnRegistration == null) throw new IllegalStateException("Registration is null.");
        if (gameOnApplicationConfiguration == null) throw new IllegalStateException("Configuration is null.");

        if (gameOnSession == null) throw new IllegalStateException("Session is null.");
        if (gameOnSession.getProfile() == null) throw new IllegalStateException("Profile must be specified.");

        if (gameOnSession.getAppBuildType() == null) throw new IllegalStateException("App build type is null.");
        if (gameOnSession.getDeviceOSType() == null) throw new IllegalStateException("Device OS Type is null.");

        if (gameOnApplicationConfiguration.getPublicKey() == null) {
            if (gameOnRegistration.getPlayerToken() == null) throw new IllegalStateException("player token is null");
            return new V1StandardSecurityAuthenticationInvoker(getClient(), gameOnSession, gameOnRegistration, gameOnApplicationConfiguration);
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
