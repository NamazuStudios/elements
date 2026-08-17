package dev.getelements.elements.deployment.jetty.fabric;

import com.google.inject.AbstractModule;
import dev.getelements.elements.cluster.fabric.guice.FabricGuiceModule;
import org.eclipse.jetty.server.Handler;

import static com.google.inject.name.Names.named;

/**
 * Wires the Fabric WebSocket transport prototype (issue #10) into a Jetty bootstrap injector. Installs the
 * transport bindings from {@code cluster-fabric-guice} and eagerly registers {@link FabricWsBootstrap}, which
 * performs the actual one-time endpoint registration as a constructor side effect.
 */
public class FabricWsModule extends AbstractModule {

    @Override
    protected void configure() {

        install(new FabricGuiceModule());

        bind(Handler.Sequence.class)
                .annotatedWith(named(FabricWsBootstrap.HANDLER_SEQUENCE))
                .toProvider(Handler.Sequence::new)
                .asEagerSingleton();

        bind(FabricWsBootstrap.class).asEagerSingleton();

    }

}
