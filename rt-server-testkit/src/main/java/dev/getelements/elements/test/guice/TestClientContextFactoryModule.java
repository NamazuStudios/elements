package dev.getelements.elements.test.guice;

import com.google.inject.*;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.sdk.guice.GuiceServiceLocatorModule;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.rt.remote.CachingContextFactory;
import dev.getelements.elements.rt.remote.RemoteInvocationDispatcher;
import dev.getelements.elements.rt.remote.SimpleRemoteInvocationDispatcher;
import dev.getelements.elements.rt.remote.guice.ClusterContextModule;

import java.util.function.Function;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.REMOTE;

public class TestClientContextFactoryModule extends PrivateModule {
    @Override
    protected void configure() {

        expose(Context.Factory.class);

        bind(Context.Factory.class)
                .to(CachingContextFactory.class)
                .asEagerSingleton();

        final var injectorProvider = getProvider(Injector.class);

        bind(new TypeLiteral<Function<DeploymentId, Context>>(){}).toProvider(() -> deploymentId -> {

            final var module = new AbstractModule() {
                @Override
                protected void configure() {

                    install(new ClusterContextModule());
                    install(new GuiceServiceLocatorModule());
                    bind(DeploymentId.class).toInstance(deploymentId);

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
