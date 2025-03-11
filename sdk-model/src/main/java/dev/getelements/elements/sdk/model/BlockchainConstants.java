package dev.getelements.elements.sdk.model;

import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;

import java.math.BigInteger;

public interface BlockchainConstants {

    /**
     * The status of the mint request
     */
    enum MintStatus {
        NOT_MINTED,
        MINTED,
        MINT_FAILED,
        MINT_PENDING
    }

    /**
     * Names of the supported blockchains.
     *
     * @deprecated use {@link BlockchainNetwork} instead
     */
    @Deprecated
    interface Names {

        /**
         * The name of the NEO blockchain
         * @deprecated use {@link BlockchainNetwork} instead
         */
        @Deprecated
        String NEO = "NEO";

        /**
         * The name of the BSC blockchain
         * @deprecated use {@link BlockchainNetwork} instead
         */
        @Deprecated
        String BSC = "BSC";

    }

    /**
     * Smart Contract Constants
     *
     * @deprecated migrate this to a configurable parameter
     */
    @Deprecated
    interface SmartContracts {

        /**
         * Gas Price
         *
         * @deprecated migrate this to a configurable parameter
         */
        @Deprecated
        BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);

        /**
         * Gas Limits
         *
         * @deprecated migrate this to a configurable parameter
         */
        @Deprecated
        BigInteger GAS_LIMIT = BigInteger.valueOf(6721975);

    }

}
