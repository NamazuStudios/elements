package com.namazustudios.socialengine.model.blockchain;

import static java.lang.String.format;

/**
 * Enumerates the systems supported blockchains.
 */
public enum BlockchainNetwork {

    /**
     * Neo Blockchain Network.
     */
    NEO(BlockchainProtocol.NEO),

    /**
     * Neo Blockchain Test Network.
     */
    NEO_TEST(BlockchainProtocol.NEO),

    /**
     * The Ethereum Main Net
     */
    ETHEREUM(BlockchainProtocol.ETHEREUM),

    /**
     * The Ethereum Test Net
     */
    ETHEREUM_TEST(BlockchainProtocol.ETHEREUM),

    /**
     * Binance Smart Chain Network.
     */
    BSC(BlockchainProtocol.ETHEREUM),

    /**
     * Binance Smart Chain Test Network.
     */
    BSC_TEST(BlockchainProtocol.ETHEREUM),

    /**
     * The Polygon Main Net
     */
    POLYGON(BlockchainProtocol.ETHEREUM),

    /**
     * The Polygon Test Net
     */
    POLYGON_TEST(BlockchainProtocol.ETHEREUM),

    /**
     * The Solana Network
     */
    SOLANA(BlockchainProtocol.SOLANA),

    /**
     * The Solana Test Network
     */
    SOLANA_TEST(BlockchainProtocol.SOLANA);

    public final BlockchainProtocol protocol;

    BlockchainNetwork(BlockchainProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * The name of the URLs configured for the network.
     *
     * @return the urls
     */
    public String urlsName() {
        return format("com.namazustudios.socialengine.blockchain.network.%s.urls", toString().toLowerCase());
    }

    /**
     * Gets the network's protocol.
     *
     * @return the network's protocol.
     */
    public BlockchainProtocol protocol() {
        return protocol;
    }

}
