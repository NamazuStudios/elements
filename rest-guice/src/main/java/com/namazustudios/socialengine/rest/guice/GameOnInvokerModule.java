package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.gameon.client.invoker.*;
import com.namazustudios.socialengine.service.gameon.client.invoker.builder.*;

public class GameOnInvokerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GameOnRegistrationInvoker.Builder.class).to(DefaultGameOnRegistrationInvokerBuilder.class);
        bind(GameOnAuthenticationInvoker.Builder.class).to(DefaultGameOnAuthenticationInvokerBuilder.class);
        bind(GameOnTournamentInvoker.Builder.class).to(DefaultGameOnTournamentInvokerBuilder.class);
        bind(GameOnPlayerTournamentInvoker.Builder.class).to(DefaultGameOnPlayerTournamentInvokerBuilder.class);
        bind(GameOnMatchInvoker.Builder.class).to(DefaultGameOnMatchInvokerBuilder.class);
        bind(GameOnAdminPrizeInvoker.Builder.class).to(DefaultGameOnPrizeInvokerBuilder.class);
        bind(GameOnGamePrizeInvoker.Builder.class).to(DefaultGameOnGamePrizeInvokerBuilder.class);
    }
}
