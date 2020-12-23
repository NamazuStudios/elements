package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.remote.CachingContextFactory;
import com.namazustudios.socialengine.rt.remote.ClusterContext;

import java.util.function.Function;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.REMOTE;

public class ClusterContextFactoryModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(Context.Factory.class);

        bind(Context.Factory.class)
            .to(CachingContextFactory.class)
            .asEagerSingleton();

        final var injectorProvider = getProvider(Injector.class);

        bind(new TypeLiteral<Function<ApplicationId, Context>>(){}).toProvider(() -> applicationId -> {

            final var module = new AbstractModule() {
                @Override
                protected void configure() {
                    install(new ClusterContextModule());
                    install(new GuiceIoCResolverModule());
                    bind(ApplicationId.class).toInstance(applicationId);
                }
            };

            final var key = Key.get(Context.class, named(REMOTE));
            final var injector = injectorProvider.get().createChildInjector(module);

            return injector.getInstance(key);

        });

    }

}
