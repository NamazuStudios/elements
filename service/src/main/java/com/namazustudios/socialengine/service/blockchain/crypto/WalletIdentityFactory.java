package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;

/**
 * Creates instances of {@link WalletAccount} securely.
 */
public interface WalletIdentityFactory {

    /**
     * Creates new {@link Wallet} with a single default identity.
     *
     * @param wallet the wallet
     * @return a new instance of the {@link Wallet}
     */
    default Wallet create(final Wallet wallet) {
        return create(wallet, 1);
    }

    /**
     * Creates new {@link Wallet} with the number of identities, setting the first as the default amount.
     *
     * @param wallet the wallet
     * @param count the number to create (must be > 0)
     * @return a new instance of the {@link Wallet}
     */
    Wallet create(final Wallet wallet, int count);

    /**
     * Gets the {@link IdentityGenerator} for the supplied {@link BlockchainApi}.
     *
     * @return the {@link IdentityGenerator}
     */
    IdentityGenerator getGenerator(BlockchainApi protocol);

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
