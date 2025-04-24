package dev.getelements.elements.sdk.model.blockchain;

import static java.lang.String.format;

/**
 * Enumerates the systems supported blockchains.
 */
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
     * The Ethereum Main Net. Uses the {@link BlockchainApi#ETHEREUM} API.
     */
    ETHEREUM(BlockchainApi.ETHEREUM),

    /**
     * The Ethereum Test Net. Uses the {@link BlockchainApi#ETHEREUM} API.
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
    SOLANA_TEST(BlockchainApi.SOLANA),

    /**
     * The Flow blockchain network.
     */
    FLOW(BlockchainApi.FLOW),

    /**
     * The Flow blockchain test network.
     */
    FLOW_TEST(BlockchainApi.FLOW),

    /**
     * The NEAR Network. Uses the {@link BlockchainApi#NEAR} API.
     */
    NEAR(BlockchainApi.NEAR),

    /**
     * The NEAR Test Network. Uses the {@link BlockchainApi#NEAR} API.
     */
    NEAR_TEST(BlockchainApi.NEAR);

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
        return format("dev.getelements.elements.blockchain.network.%s", toString().toLowerCase());
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
