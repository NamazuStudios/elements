package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.model.blockchain.wallet.VaultKey;
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
     * @return a {@link Wallet} instance encry
     */
    Wallet encrypt(Wallet wallet);

    /**
     * Decrypts the wallet. If decryption fails, such as for bad passphrase, then the method will return
     * {@link Optional#empty()}
     *
     *
     * @param unencryptedVaultKey
     * @param wallet the {@link Wallet}
     * @return the {@link Optional<Wallet>}
     */
    Optional<Wallet> decrypt(VaultKey unencryptedVaultKey, Wallet wallet);

    /**
     * Decrypts the wallet. If decryption fails, such as for bad passphrase, then the method will return
     * {@link Optional#empty()}
     *
     *
     * @param unencryptedVaultKey
     * @param encryptedWalletAccount the {@link WalletAccount}
     * @return the {@link Optional<Wallet>}
     */
    Optional<WalletAccount> decrypt(VaultKey unencryptedVaultKey, WalletAccount encryptedWalletAccount);

}
