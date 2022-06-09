package com.namazustudios.socialengine;

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
     * The field type of the field inside MetadataSpec
     */
    enum TemplateFieldType {
        String,
        Number,
        Boolean,
        Array,
        Enum,
        Object,
        Tags
    }

    /**
     * Names of the supported blockchains
     */
    interface Names {

        /**
         * The name of the NEO blockchain
         */
        String NEO = "NEO";

        /**
         * The name of the BSC blockchain
         */
        String BSC = "BSC";

    }

    interface Topics {

        /**
         * The root topic of the NeoApplicationLog type
         */
        String NEO_APPLICATION_LOG = "neoapplicationlog";
        String BSC_APPLICATION_LOG = "bscapplicationlog";
    }

    interface SmartContracts{
        BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
        BigInteger GAS_LIMIT = BigInteger.valueOf(6721975);
    }

}
