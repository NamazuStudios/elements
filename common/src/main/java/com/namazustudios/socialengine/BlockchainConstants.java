package com.namazustudios.socialengine;

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
     * Names of the supported blockchains
     */
    interface Names {

        /**
         * The name of the NEO blockchain
         */
        String NEO = "NEO";

    }

    interface Topics {

        /**
         * The root topic of the NeoApplicationLog type
         */
        String NEO_APPLICATION_LOG = "neoapplicationlog";
    }

}
