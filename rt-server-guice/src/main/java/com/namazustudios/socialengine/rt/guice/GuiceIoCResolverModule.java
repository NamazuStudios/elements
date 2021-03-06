package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.IocResolver;

/**
 * Bridges the gap between a Guice {@link com.google.inject.Injector} and an {@link IocResolver}.
 */
public class GuiceIoCResolverModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(IocResolver.class);
        bind(GuiceIoCResolver.class);
        bind(IocResolver.class).to(GuiceIoCResolver.class);
    }

}
