package dev.getelements.elements.cluster.fabric.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.cluster.fabric.JakartaWebsocketInstanceConnectionService;
import dev.getelements.elements.rt.remote.InstanceConnectionService;
import dev.getelements.elements.rt.remote.InstanceDiscoveryService;
import dev.getelements.elements.rt.remote.RemoteInvoker;
import dev.getelements.elements.sdk.cluster.id.InstanceId;

/**
 * Binds {@link InstanceConnectionService} to the Fabric WebSocket transport's
 * {@link JakartaWebsocketInstanceConnectionService} (issue #10). Kept separate from {@link FabricGuiceModule} so
 * narrow transport-only usages (e.g. the round-trip test) don't have to also satisfy {@link InstanceId}/
 * {@link InstanceDiscoveryService} bindings they have no use for.
 *
 * <p>Requires {@link InstanceId}, {@link InstanceDiscoveryService}, and {@link RemoteInvoker} to already be bound
 * (the latter via {@link FabricGuiceModule}, which must also be installed).</p>
 */
public class JakartaWebsocketInstanceConnectionServiceModule extends AbstractModule {

    @Override
    protected void configure() {

        requireBinding(InstanceId.class);
        requireBinding(InstanceDiscoveryService.class);
        requireBinding(RemoteInvoker.class);

        bind(InstanceConnectionService.class)
                .to(JakartaWebsocketInstanceConnectionService.class)
                .asEagerSingleton();

    }

}
