package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.gameon.client.invoker.*;

public class GameOnInvokerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GameOnRegistrationInvoker.Builder.class).to(DefaultGameOnRegistrationInvokerBuilder.class);
        bind(GameOnAuthenticationInvoker.Builder.class).to(DefaultGameOnAuthenticationInvokerBuilder.class);
        bind(GameOnTournamentInvoker.Builder.class).to(DefaultGameOnTournamentInvokerBuilder.class);
        bind(GameOnMatchInvoker.Builder.class).to(DefaultGameOnMatchInvokerBuilder.class);
    }
}
