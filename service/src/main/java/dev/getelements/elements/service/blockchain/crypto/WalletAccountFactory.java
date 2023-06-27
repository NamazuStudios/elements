package dev.getelements.elements.service.blockchain.crypto;

import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.blockchain.wallet.WalletAccount;

/**
 * Creates instances of {@link WalletAccount} securely.
 */
public interface WalletAccountFactory {

    /**
     * Gets the {@link AccountGenerator} for the supplied {@link BlockchainApi}.
     *
     * @return the {@link AccountGenerator}
     */
    AccountGenerator getGenerator(BlockchainApi api);

    /**
     * Generates instances of {@link WalletAccount}.
     */
    @FunctionalInterface
    interface AccountGenerator {

        /**
         * Generates a {@link WalletAccount} unencrypted in raw form.
         *
         * @return the {@link WalletAccount}
         */
        WalletAccount generate();

    }

}
