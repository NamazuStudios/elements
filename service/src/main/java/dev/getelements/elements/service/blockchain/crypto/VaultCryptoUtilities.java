package dev.getelements.elements.service.blockchain.crypto;

import dev.getelements.elements.model.blockchain.wallet.Vault;
import dev.getelements.elements.model.blockchain.wallet.VaultKey;
import dev.getelements.elements.model.crypto.PrivateKeyCrytpoAlgorithm;

import java.util.Optional;

/**
 * Encrypts and decrypts {@link Vault}s.
 */
public interface VaultCryptoUtilities {

    /**
     * Generates a {@link VaultKey}, unencrypted.
     *
     * @return the {@link VaultKey}
     */
    VaultKey generateKey(PrivateKeyCrytpoAlgorithm algorithm);

    /**
     * Generates a {@link VaultKey} secured with the supplied passphrase.
     *
     * @param passphrase the passphrase
     * @return the {@link VaultKey}, encrypted
     */
    default VaultKey generateKey(final PrivateKeyCrytpoAlgorithm algorithm, final String passphrase) {
        final var key = generateKey(algorithm);
        return encryptKey(key, passphrase);
    }

    /**
     * Encrypts the supplied {@link VaultKey} with the supplied passphrase.
     *
     * @param vaultKey the {@link VaultKey}, must not be encrypted
     * @param passphrase the passphrase.
     * @return the encrypted key
     */
    VaultKey encryptKey(VaultKey vaultKey, String passphrase);

    /**
     * Re-encrypts the {@link VaultKey} with the supplied passphrase.
     *
     * @param key the key
     * @param passphrase the existing passphrase
     * @param newPassphrase the new passphrase
     *
     * @return an {@link Optional<VaultKey>}, if re-encryption was successful
     */
    default Optional<VaultKey> reEncryptKey(VaultKey key, String passphrase, String newPassphrase) {
        return decryptKey(key, passphrase).map(decrypted -> encryptKey(decrypted, newPassphrase));
    }

    /**
     * Returns the {@link VaultKey} unencrypted.
     *
     * @param key the vault key
     * @param passphrase the passphrase
     * @return an {@link Optional<VaultKey>}, or null.
     */
    Optional<VaultKey> decryptKey(VaultKey key, String passphrase);

}
