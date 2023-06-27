package dev.getelements.elements.model.blockchain;

import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.rt.annotation.RemoteModel;
import dev.getelements.elements.rt.annotation.RemoteScope;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.getelements.elements.rt.annotation.RemoteScope.API_SCOPE;
import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;
import static java.lang.String.format;

/**
 * Enumerates the various blockchain protocols supported by elements.
 */
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public enum BlockchainApi {

    /**
     * The Neo API.
     */
    NEO(ELEMENTS_JSON_RPC_PROTOCOL),

    /**
     * The Ethereum API.
     */
    ETHEREUM(ELEMENTS_JSON_RPC_PROTOCOL),

    /**
     * The Solana protocol.
     */
    SOLANA(ELEMENTS_JSON_RPC_PROTOCOL),

    /**
     * The Flow API.
     */
    FLOW;

    /**
     * Lists all protocols supported by this {@link BlockchainApi}.
     */
    private final Set<String> protocols;

    BlockchainApi(final String ... protocols) {
        this.protocols = Set.of(protocols);
    }

    /**
     * Validates that one or more network matches the
     *
     * @param networks the {@link Collection<BlockchainNetwork>}
     */
    public void validate(final Collection<BlockchainNetwork> networks) {
        validate(networks, InvalidDataException::new);
    }

    /**
     * Validates that one or more network matches the
     *
     * @param networks the {@link Collection<BlockchainNetwork>}
     * @param exSupplier this {@link Supplier<ExceptionT>}
     * @param <ExceptionT> the exception to throw if the validation fails
     */
    public <ExceptionT extends Throwable>
    void validate(
            final Collection<BlockchainNetwork> networks,
            final Supplier<ExceptionT> exSupplier) throws ExceptionT {
        for (var network : networks) {
            if (network == null) {
                throw exSupplier.get();
            } else if (!this.equals(network.api())) {
                throw exSupplier.get();
            }
        }
    }

    /**
     * Gets all {@link BlockchainNetwork}s associated with this API.
     *
     * @return a {@link Stream<BlockchainNetwork>}
     */
    public Stream<BlockchainNetwork> networks() {
        return Stream.of(BlockchainNetwork.values()).filter(network -> this.equals(network.api()));
    }

    /**
     * Gets the protocols, as specified in the {@link RemoteScope#protocol()} annotation.
     *
     * @return the protocols supported by this API
     */
    public Set<String> getApis() {
        return protocols;
    }

}
