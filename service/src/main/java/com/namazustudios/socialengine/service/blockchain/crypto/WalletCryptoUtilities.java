package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;

import java.util.Optional;

/**
 * Encrypts and decrypts {@link Wallet}s.
 */
public interface WalletCryptoUtilities {

    /**
     * Encrypts the {@link Wallet}. If any {@link WalletAccount} whose {@link WalletAccount#isEncrypted()}
     * flag is set to true, this method will throw an exception.
     *
     * @param wallet the wallet contents itself
     * @param passphrase the passphrase used to secure the wallet
     * @return a {@link Wallet} instance encry
     */
    Wallet encrypt(Wallet wallet, String passphrase);

    /**
     * Decrypts and and immediately encrypts the wallet. If decryption fails, such as for bad passphrase, then the
     * method will return {@link Optional#empty()}
     *
     * @param wallet the {@link Wallet}
     * @param passphrase the existing passphrase
     * @param newPassphrase the new passphrase
     * @return the {@link Optional<Wallet>}
     */
    default Optional<Wallet> reEncrypt(final Wallet wallet, final String passphrase, final String newPassphrase) {
        return decrypt(wallet, passphrase).map(w -> encrypt(w, newPassphrase));
    }

    /**
     * Decrypts the wallet. If decryption fails, such as for bad passphrase, then the method will return
     * {@link Optional#empty()}
     *
     * @param wallet the {@link Wallet}
     * @param passphrase the existing passphrase
     * @return the {@link Optional<Wallet>}
     */
    Optional<Wallet> decrypt(Wallet wallet, String passphrase);

}
