package com.namazustudios.socialengine.service.blockchain.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.blockchain.wallet.VaultKey;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;
import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;
import com.namazustudios.socialengine.service.util.CipherUtility;
import com.namazustudios.socialengine.service.util.CryptoKeyPairUtility;

import javax.inject.Inject;
import javax.validation.Validator;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class StandardWalletCryptoUtilities implements WalletCryptoUtilities {

    private Validator validator;

    private ObjectMapper objectMapper;

    private CipherUtility cipherUtility;

    private CryptoKeyPairUtility cryptoKeyPairUtility;

    @Override
    public Wallet encrypt(final Wallet wallet) {

        final var vaultKey = getVaultKey(wallet);

        final var publicKey = getCryptoKeyPairUtility().getPublicKey(
                vaultKey.getAlgorithm(),
                vaultKey.getPublicKey()
        );

        final var encryptedAccounts = wallet
                .getAccounts()
                .stream()
                .map(a -> doEncrypt(vaultKey.getAlgorithm(), publicKey, a))
                .collect(toList());

        final var encryptedWallet = getObjectMapper().convertValue(wallet, Wallet.class);
        encryptedWallet.setAccounts(encryptedAccounts);

        return encryptedWallet;

    }

    private WalletAccount doEncrypt(
            final PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
            final PublicKey publicKey,
            final WalletAccount walletAccount) {

        if (walletAccount == null) {
            throw new InvalidDataException("Account must not be null.");
        } else if (walletAccount.isEncrypted()) {
            throw new InvalidDataException("Account must be unencrypted.");
        }

        final var accountPrivateKey = walletAccount.getPrivateKey();

        final var encryptedAccountPrivateKey = getCipherUtility().encrypt(
                privateKeyCrytpoAlgorithm,
                publicKey,
                accountPrivateKey
        );

        final var encryptedAccount = getObjectMapper().convertValue(walletAccount, WalletAccount.class);
        encryptedAccount.setEncrypted(true);
        encryptedAccount.setPrivateKey(encryptedAccountPrivateKey);

        return encryptedAccount;

    }

    @Override
    public Optional<Wallet> decrypt(final VaultKey unencryptedVaultKey, final Wallet wallet) {

        final var vaultKey = getVaultKey(wallet);

        if (!Objects.equals(vaultKey.getPublicKey(), unencryptedVaultKey.getPublicKey())) {
            throw new IllegalArgumentException("Vault key mismatch.");
        }

        if (unencryptedVaultKey.isEncrypted()) {
            throw new IllegalArgumentException("Vault is encrypted.");
        }

        final var privateKey = getCryptoKeyPairUtility().getPrivateKey(
                vaultKey.getAlgorithm(),
                vaultKey.getPrivateKey()
        );

        final var decryptedAccounts = new ArrayList<WalletAccount>();

        for (var encryptedAccount : wallet.getAccounts()) {

            if (!encryptedAccount.isEncrypted()) {
                throw new IllegalArgumentException("Account must be encrypted.");
            }

            final var encryptedPrivateKey = encryptedAccount.getPrivateKey();

            if (encryptedPrivateKey == null) {
                throw new IllegalArgumentException("Account must have a private key.");
            }

            final var decryptedPrivateKey = getCipherUtility().decryptString(
                    vaultKey.getAlgorithm(),
                    privateKey,
                    encryptedPrivateKey
            );

            if (decryptedPrivateKey.isEmpty()) {
                return Optional.empty();
            }

            final var decryptedAccount = getObjectMapper().convertValue(
                    encryptedAccount,
                    WalletAccount.class
            );

            decryptedAccount.setEncrypted(false);
            decryptedAccount.setPrivateKey(decryptedPrivateKey.get());

            decryptedAccounts.add(decryptedAccount);

        }

        final var decryptedWallet = getObjectMapper().convertValue(wallet, Wallet.class);
        decryptedWallet.setAccounts(decryptedAccounts);

        return Optional.of(decryptedWallet);

    }

    @Override
    public Optional<WalletAccount> decrypt(final VaultKey unencryptedVaultKey,
                                           final WalletAccount encryptedWalletAccount) {

        if (unencryptedVaultKey.isEncrypted()) {
            throw new IllegalArgumentException("Vault is encrypted.");
        }

        final var privateKey = getCryptoKeyPairUtility().getPrivateKey(
                unencryptedVaultKey.getAlgorithm(),
                unencryptedVaultKey.getPrivateKey()
        );

        if (!encryptedWalletAccount.isEncrypted()) {
            throw new IllegalArgumentException("Account must be encrypted.");
        }

        final var encryptedPrivateKey = encryptedWalletAccount.getPrivateKey();

        if (encryptedPrivateKey == null) {
            throw new IllegalArgumentException("Account must have a private key.");
        }

        final var decryptedPrivateKey = getCipherUtility().decryptString(
                unencryptedVaultKey.getAlgorithm(),
                privateKey,
                encryptedPrivateKey
        );

        if (decryptedPrivateKey.isEmpty()) {
            return Optional.empty();
        }

        final var decryptedAccount = getObjectMapper().convertValue(
                encryptedWalletAccount,
                WalletAccount.class
        );

        decryptedAccount.setEncrypted(false);
        decryptedAccount.setPrivateKey(decryptedPrivateKey.get());

        return Optional.of(decryptedAccount);

    }

    private VaultKey getVaultKey(final Wallet wallet) {

        if (!getValidator().validateProperty(wallet, "vault").isEmpty()) {
            throw new IllegalArgumentException("Invalid wallet.");
        }

        if (!getValidator().validateProperty(wallet, "accounts").isEmpty()) {
            throw new IllegalArgumentException("Invalid wallet.");
        }

        final var vault = wallet.getVault();

        if (!getValidator().validate(vault).isEmpty()) {
            throw new IllegalArgumentException("Invalid vault.");
        }

        return vault.getKey();

    }

    public Validator getValidator() {
        return validator;
    }

    @Inject
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CipherUtility getCipherUtility() {
        return cipherUtility;
    }

    @Inject
    public void setCipherUtility(CipherUtility cipherUtility) {
        this.cipherUtility = cipherUtility;
    }

    public CryptoKeyPairUtility getCryptoKeyPairUtility() {
        return cryptoKeyPairUtility;
    }

    @Inject
    public void setCryptoKeyPairUtility(CryptoKeyPairUtility cryptoKeyPairUtility) {
        this.cryptoKeyPairUtility = cryptoKeyPairUtility;
    }

}
