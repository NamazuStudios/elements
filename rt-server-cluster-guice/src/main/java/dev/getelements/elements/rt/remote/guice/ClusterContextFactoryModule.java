package dev.getelements.elements.rt.remote.guice;

import com.google.inject.*;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.guice.GuiceIoCResolverModule;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.remote.CachingContextFactory;
import dev.getelements.elements.rt.remote.RemoteInvocationDispatcher;
import dev.getelements.elements.rt.remote.SimpleRemoteInvocationDispatcher;

import java.util.function.Function;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.REMOTE;

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

                    bind(RemoteInvocationDispatcher.class)
                        .to(SimpleRemoteInvocationDispatcher.class)
                        .asEagerSingleton();

                }
            };

            final var key = Key.get(Context.class, named(REMOTE));
            final var injector = injectorProvider.get().createChildInjector(module);

            return injector.getInstance(key);

        });

    }

}
