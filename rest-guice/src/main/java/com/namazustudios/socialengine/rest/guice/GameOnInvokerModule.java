package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.gameon.client.invoker.DefaultGameOnRegistrationInvokerBuilder;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnRegistrationInvoker;

public class GameOnInvokerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GameOnRegistrationInvoker.Builder.class).to(DefaultGameOnRegistrationInvokerBuilder.class);
    }
}
