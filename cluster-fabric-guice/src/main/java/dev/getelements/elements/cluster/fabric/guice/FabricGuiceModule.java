package dev.getelements.elements.cluster.fabric.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.cluster.fabric.FabricEndpoint;
import dev.getelements.elements.cluster.fabric.JakartaWebsocketRemoteInvoker;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import dev.getelements.elements.rt.remote.LocalInvocationDispatcher;
import dev.getelements.elements.rt.remote.RemoteInvoker;
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
 * Binds the Fabric WebSocket transport (issue #10). Dispatches against the existing (deprecated)
 * {@link ServiceLocatorLocalInvocationDispatcher}, resolving against a {@link ServiceLocator} private to this
 * module &mdash; this is a placeholder pending Phase 3's {@code ElementRegistry}-based dispatch, not the final
 * design.
 *
 * <p>A {@link PrivateModule} because {@link ServiceLocator} is never a single ambient binding in the real
 * {@code jetty-ws} graph &mdash; each installed Element gets its own private one (via
 * {@code SharedElementModule}), and a plain top-level binding here would collide with (or be rejected alongside)
 * those. Only {@link FabricEndpoint} and {@link RemoteInvoker} are exposed; the private
 * {@link ServiceLocator}/{@link LocalInvocationDispatcher} wiring stays internal to this module.</p>
 *
 * <p>Requires {@link PayloadReader}/{@link PayloadWriter} to already be bound (e.g. via
 * {@code KryoPayloadReaderWriterModule}).</p>
 */
public class FabricGuiceModule extends PrivateModule {

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
        expose(FabricEndpoint.class);

        // Unscoped: a fresh JakartaWebsocketRemoteInvoker is minted per remote instance (one per
        // InstanceConnectionService.InstanceConnection), each start()-ed against a different connect address.
        bind(RemoteInvoker.class).to(JakartaWebsocketRemoteInvoker.class);
        expose(RemoteInvoker.class);

    }

}
