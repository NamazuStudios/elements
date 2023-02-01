package com.namazustudios.socialengine.service.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;
import com.namazustudios.socialengine.service.blockchain.invoke.flow.FlowInvoker;
import org.onflow.sdk.Flow;
import org.onflow.sdk.FlowAccessApi;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.inject.name.Names.named;

public class FlowNetworkModule extends PrivateModule {

    private final BlockchainNetwork network;

    public FlowNetworkModule(final BlockchainNetwork network) {

        if (!BlockchainApi.FLOW.equals(network.api())) {
            throw new IllegalArgumentException("Must be FLOW API Network.");
        }

        this.network = network;
    }

    @Override
    protected void configure() {

        final var urlKey = Key.get(String.class, named(network.urlsName()));
        final var urlProvider = getProvider(urlKey);

        bind(FlowAccessApi.class).toProvider(() -> {

            final URI uri;

            try {
                uri = new URI(urlProvider.get());
            } catch (URISyntaxException ex) {
                throw new InternalException(ex);
            }

            return Flow.newAccessApi(uri.getHost(), uri.getPort());

        }).asEagerSingleton();

        bind(FlowSmartContractInvocationService.Invoker.class)
                .annotatedWith(named(network.iocName()))
                .to(FlowInvoker.class);

        expose(FlowSmartContractInvocationService.Invoker.class)
                .annotatedWith(named(network.iocName()));

    }

}
