package dev.getelements.elements.service.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.service.blockchain.FlowSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.invoke.flow.FlowInvoker;
import org.onflow.sdk.Flow;
import org.onflow.sdk.FlowAccessApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.blockchain.BlockchainApi.FLOW;

public class FlowNetworkModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(FlowNetworkModule.class);

    private final BlockchainNetwork network;

    public FlowNetworkModule(final BlockchainNetwork network) {

        if (!FLOW.equals(network.api())) {
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

            if (!"grpc".equals(uri.getScheme())) {
                logger.warn("Incorrect Flow scheme specification: Expected grpc but got {}.", uri.getScheme());
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
