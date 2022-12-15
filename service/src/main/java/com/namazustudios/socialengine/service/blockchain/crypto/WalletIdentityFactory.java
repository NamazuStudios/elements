package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainProtocol;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletIdentityPair;

/**
 * Creates instances of {@link WalletIdentityPair} securely.
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
     * Gets the {@link IdentityGenerator} for the supplied {@link BlockchainProtocol}.
     *
     * @return the {@link IdentityGenerator}
     */
    IdentityGenerator getGenerator(BlockchainProtocol protocol);

    /**
     * Generates instances of {@link WalletIdentityPair}.
     */
    @FunctionalInterface
    interface IdentityGenerator {

        /**
         * Generates a {@link WalletIdentityPair} unencrypted in raw form.
         *
         * @return the {@link WalletIdentityPair}
         */
        WalletIdentityPair generate();

    }

}
