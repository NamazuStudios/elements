package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.IocResolver;

/**
 * Bridges the gap between a Guice {@link com.google.inject.Injector} and an {@link IocResolver}.
 */
public class GuiceIoCResolverModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GuiceIoCResolver.class);
        bind(IocResolver.class).to(GuiceIoCResolver.class);
    }

}
