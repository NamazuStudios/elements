package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;

/**
 * Creates instances of {@link WalletAccount} securely.
 */
public interface WalletIdentityFactory {

    /**
     * Gets the {@link IdentityGenerator} for the supplied {@link BlockchainApi}.
     *
     * @return the {@link IdentityGenerator}
     */
    IdentityGenerator getGenerator(BlockchainApi api);

    /**
     * Generates instances of {@link WalletAccount}.
     */
    @FunctionalInterface
    interface IdentityGenerator {

        /**
         * Generates a {@link WalletAccount} unencrypted in raw form.
         *
         * @return the {@link WalletAccount}
         */
        WalletAccount generate();

    }

}
