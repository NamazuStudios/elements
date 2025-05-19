package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Enumerates the various blockchain protocols supported by elements.
 */
public enum BlockchainApi {

    /**
     * The Neo API.
     */
    NEO,

    /**
     * The Ethereum API.
     */
    ETHEREUM,

    /**
     * The Solana protocol.
     */
    SOLANA,

    /**
     * The Flow API.
     */
    FLOW,

    /**
     * The Flow API.
     */
    NEAR;

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

}
