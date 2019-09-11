package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.service.gameon.client.invoker.*;
import com.namazustudios.socialengine.service.gameon.client.invoker.builder.*;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class GameOnInvokerModule extends PrivateModule {
    @Override
    protected void configure() {

        expose(GameOnRegistrationInvoker.Builder.class);
        expose(GameOnAuthenticationInvoker.Builder.class);
        expose(GameOnTournamentInvoker.Builder.class);
        expose(GameOnPlayerTournamentInvoker.Builder.class);
        expose(GameOnMatchInvoker.Builder.class);
        expose(GameOnAdminPrizeInvoker.Builder.class);
        expose(GameOnGamePrizeInvoker.Builder.class);

        bind(GameOnRegistrationInvoker.Builder.class).to(DefaultGameOnRegistrationInvokerBuilder.class);
        bind(GameOnAuthenticationInvoker.Builder.class).to(DefaultGameOnAuthenticationInvokerBuilder.class);
        bind(GameOnTournamentInvoker.Builder.class).to(DefaultGameOnTournamentInvokerBuilder.class);
        bind(GameOnPlayerTournamentInvoker.Builder.class).to(DefaultGameOnPlayerTournamentInvokerBuilder.class);
        bind(GameOnMatchInvoker.Builder.class).to(DefaultGameOnMatchInvokerBuilder.class);
        bind(GameOnAdminPrizeInvoker.Builder.class).to(DefaultGameOnPrizeInvokerBuilder.class);
        bind(GameOnGamePrizeInvoker.Builder.class).to(DefaultGameOnGamePrizeInvokerBuilder.class);

    }
}
