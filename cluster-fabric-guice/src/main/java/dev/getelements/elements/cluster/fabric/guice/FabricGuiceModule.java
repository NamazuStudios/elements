package dev.getelements.elements.cluster.fabric.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.cluster.fabric.FabricEndpoint;
import dev.getelements.elements.cluster.fabric.FabricRemoteInvoker;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import dev.getelements.elements.rt.remote.LocalInvocationDispatcher;
import dev.getelements.elements.rt.remote.ServiceLocatorLocalInvocationDispatcher;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.guice.GuiceServiceLocator;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static dev.getelements.elements.rt.annotation.RemoteScope.REMOTE_PROTOCOL;
import static dev.getelements.elements.rt.annotation.RemoteScope.REMOTE_SCOPE;
import static dev.getelements.elements.rt.annotation.RemoteScope.WORKER_SCOPE;

/**
 * Binds the Fabric WebSocket transport prototype (issue #10). Dispatches against the existing (deprecated)
 * {@link ServiceLocatorLocalInvocationDispatcher}, resolving against whatever this injector has bound &mdash;
 * this is a placeholder pending Phase 3's {@code ElementRegistry}-based dispatch, not the final design.
 *
 * <p>Requires {@link PayloadReader}/{@link PayloadWriter} to already be bound (e.g. via
 * {@code KryoPayloadReaderWriterModule}).</p>
 */
public class FabricGuiceModule extends AbstractModule {

    @Override
    protected void configure() {

        requireBinding(PayloadReader.class);
        requireBinding(PayloadWriter.class);

        bind(ServiceLocator.class).toInstance(new GuiceServiceLocator());

        bind(LocalInvocationDispatcher.class)
                .to(ServiceLocatorLocalInvocationDispatcher.class);

        bind(String.class)
                .annotatedWith(named(REMOTE_SCOPE))
                .toInstance(WORKER_SCOPE);

        bind(String.class)
                .annotatedWith(named(REMOTE_PROTOCOL))
                .toInstance(ELEMENTS_RT_PROTOCOL);

        bind(FabricEndpoint.class).in(SINGLETON);
        bind(FabricRemoteInvoker.class).in(SINGLETON);

    }

}
