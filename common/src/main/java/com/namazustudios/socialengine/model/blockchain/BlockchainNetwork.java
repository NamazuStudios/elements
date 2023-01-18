package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;

import java.util.Optional;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;
import static java.lang.String.format;

/**
 * Enumerates the systems supported blockchains.
 */
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public enum BlockchainNetwork {

    /**
     * Neo Blockchain Network. Uses the {@link BlockchainApi#NEO} API.
     */
    NEO(BlockchainApi.NEO),

    /**
     * Neo Blockchain Test Network. Uses the {@link BlockchainApi#NEO} API.
     */
    NEO_TEST(BlockchainApi.NEO),

    /**
     * The Ethereum Main Net. Uses the  Uses the {@link BlockchainApi#ETHEREUM} API.
     */
    ETHEREUM(BlockchainApi.ETHEREUM),

    /**
     * The Ethereum Test Net. Uses the  Uses the {@link BlockchainApi#ETHEREUM} API.
     */
    ETHEREUM_TEST(BlockchainApi.ETHEREUM),

    /**
     * Binance Smart Chain Network. Uses the {@link BlockchainApi#ETHEREUM} API.
     */
    BSC(BlockchainApi.ETHEREUM),

    /**
     * Binance Smart Chain Test Network. Uses the {@link BlockchainApi#ETHEREUM} API.
     */
    BSC_TEST(BlockchainApi.ETHEREUM),

    /**
     * The Polygon Main Net. Uses the {@link BlockchainApi#ETHEREUM} API.
     */
    POLYGON(BlockchainApi.ETHEREUM),

    /**
     * The Polygon Test Net. Uses the {@link BlockchainApi#ETHEREUM} API.
     */
    POLYGON_TEST(BlockchainApi.ETHEREUM),

    /**
     * The Solana Network. Uses the {@link BlockchainApi#SOLANA} API.
     */
    SOLANA(BlockchainApi.SOLANA),

    /**
     * The Solana Test Network. Uses the {@link BlockchainApi#SOLANA} API.
     */
    SOLANA_TEST(BlockchainApi.SOLANA);

    private final BlockchainApi api;

    BlockchainNetwork(BlockchainApi api) {
        this.api = api;
    }

    /**
     * The IoC Name for anything related to the blockchain network. Used to dynamically resolve components in the
     * system for anything. This also serves as the base IoC name for external configuration.
     *
     * It always carries the same format.
     *
     * @return the urls
     */
    public String iocName() {
        return format("com.namazustudios.socialengine.blockchain.network.%s", toString().toLowerCase());
    }

    /**
     * The name of the URLs configured for the network.
     *
     * @return the urls
     */
    public String urlsName() {
        return format("%s.urls", iocName());
    }

    /**
     * Gets the network's protocol.
     *
     * @return the network's protocol.
     */
    public BlockchainApi api() {
        return api;
    }

}
