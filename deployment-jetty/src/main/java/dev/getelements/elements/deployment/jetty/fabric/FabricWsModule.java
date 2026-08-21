package dev.getelements.elements.deployment.jetty.fabric;

import com.google.inject.AbstractModule;
import org.eclipse.jetty.server.Handler;

import static com.google.inject.name.Names.named;

/**
 * Registers the Fabric WebSocket transport's (issue #10) {@code /cluster/v1} HTTP endpoint by eagerly constructing
 * {@link FabricWsBootstrap}. The transport bindings themselves ({@code FabricGuiceModule}) are installed globally
 * by {@code ElementsCoreModule} rather than here, since they must also be visible to cluster-core singletons
 * (e.g. {@code SimpleInstance}) outside this module's private servlet-context scope.
 */
public class FabricWsModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(Handler.Sequence.class)
                .annotatedWith(named(FabricWsBootstrap.HANDLER_SEQUENCE))
                .toProvider(Handler.Sequence::new)
                .asEagerSingleton();

        bind(FabricWsBootstrap.class).asEagerSingleton();

    }

}
