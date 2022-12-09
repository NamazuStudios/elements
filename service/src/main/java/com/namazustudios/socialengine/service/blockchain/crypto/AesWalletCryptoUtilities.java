package com.namazustudios.socialengine.service.blockchain.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.crypto.CryptoException;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletIdentityPair;
import com.namazustudios.socialengine.rt.util.Hex;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class AesWalletCryptoUtilities implements WalletCryptoUtilities {

    private static final int IV_LENGTH = 16;

    private static final int SALT_LENGTH = 32;

    private static final int AES_KEY_LENGTH = 256;

    private static final int AES_ITERATIONS = 65536;

    private static final String AES = "AES";

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final SecureRandom sr = new SecureRandom();

    private Validator validator;

    private ObjectMapper objectMapper;

    @Override
    public Wallet encrypt(final Wallet wallet, final String passphrase) {

        final var iv = new byte[IV_LENGTH];
        final var salt = new byte[SALT_LENGTH];
        sr.nextBytes(iv);
        sr.nextBytes(salt);

        final var aesEncryptionSpec = new AesEncryptionSpec();
        aesEncryptionSpec.setFamily(AES);
        aesEncryptionSpec.setIv(Hex.encode(iv));
        aesEncryptionSpec.setSalt(Hex.encode(salt));
        aesEncryptionSpec.setAlgorithm(AES_ALGORITHM);
        aesEncryptionSpec.setSecretKeyAlgorithm(SECRET_KEY_ALGORITHM);
        aesEncryptionSpec.setIterations(AES_ITERATIONS);
        aesEncryptionSpec.setKeyLength(AES_KEY_LENGTH);

        if (!getValidator().validate(aesEncryptionSpec).isEmpty()) {
            throw new InternalException("Generated invalid AES Encryption Spec.");
        }

        final var encryption = getObjectMapper().convertValue(aesEncryptionSpec, Map.class);
        wallet.setEncryption(encryption);

        final var encrypted = wallet
                .getIdentities()
                .stream()
                .map(identity -> doEncrypt(iv, salt, passphrase, aesEncryptionSpec, identity))
                .collect(toList());

        final var result = getObjectMapper().convertValue(wallet, Wallet.class);
        result.setIdentities(encrypted);

        return result;

    }

    private WalletIdentityPair doEncrypt(final byte[] iv, final byte [] salt,
                                         final String passphrase,
                                         final AesEncryptionSpec aesEncryptionSpec,
                                         final WalletIdentityPair identity) {

        if (identity.isEncrypted()) {
            throw new IllegalArgumentException("Identity is already encrypted.");
        }

        final var unencrypted = identity.getPrivateKey();

        if (unencrypted == null) {
            throw new IllegalArgumentException("Account must not be null.");
        }

        final var encrypted = getObjectMapper().convertValue(identity, WalletIdentityPair.class);

        try {
            final var cipher = getCipher(iv, salt, passphrase, aesEncryptionSpec, Cipher.ENCRYPT_MODE);
            final var unencryptedBytes = unencrypted.getBytes(StandardCharsets.UTF_8);
            final var encryptedBytes = cipher.doFinal(unencryptedBytes);
            final var encryptedString = Hex.encode(encryptedBytes);
            encrypted.setPrivateKey(encryptedString);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            throw new CryptoException(ex);
        }

        return encrypted;

    }

    @Override
    public Optional<Wallet> decrypt(final Wallet wallet, final String passphrase) {

        final var encryption = wallet.getEncryption();

        if (encryption == null) {
            throw new IllegalArgumentException("No encryption metadata.");
        }

        final var aesEncryptionSpec = getObjectMapper().convertValue(encryption, AesEncryptionSpec.class);
        final var violations = getValidator().validate(aesEncryptionSpec);

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid AES encryption metadata.");
        }

        final var iv = Hex.decode(aesEncryptionSpec.getIv());
        final var salt = Hex.decode(aesEncryptionSpec.getSalt());

        final var decrypted = wallet
                .getIdentities()
                .stream()
                .map(identity -> doDecrypt(iv, salt, passphrase, aesEncryptionSpec, identity))
                .collect(toList());

        return Optional.empty();
    }

    public WalletIdentityPair doDecrypt(byte[] iv, byte[] salt,
                                        final String passphrase,
                                        final AesEncryptionSpec aesEncryptionSpec,
                                        final WalletIdentityPair identity) {

        final var decrypted = getObjectMapper().convertValue(identity, WalletIdentityPair.class);
        decrypted.setEncrypted(false);

        try {
            final var encrypted = identity.getPrivateKey();
            final var cipher = getCipher(iv, salt, passphrase, aesEncryptionSpec, Cipher.DECRYPT_MODE);
            final var encryptedBytes = Hex.decode(encrypted);
            final var decryptedBytes = cipher.doFinal(encryptedBytes);
            final var decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
            decrypted.setPrivateKey(decryptedString);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            throw new CryptoException(ex);
        }

        return decrypted;

    }

    private Cipher getCipher(final byte[] iv, final byte[] salt,
                             final String passphrase,
                             final AesEncryptionSpec aesEncryptionSpec, final int mode) {
        try {

            final var chars = passphrase.toCharArray();
            final var keySpec = new PBEKeySpec(chars, salt, aesEncryptionSpec.getIterations(), aesEncryptionSpec.getKeyLength());
            final var factory = SecretKeyFactory.getInstance(aesEncryptionSpec.getSecretKeyAlgorithm());
            final var secret = factory.generateSecret(keySpec);
            final var secretKeySpec = new SecretKeySpec(secret.getEncoded(), aesEncryptionSpec.getFamily());
            final var ivParameterSpec = new IvParameterSpec(iv);
            final var cipher = Cipher.getInstance(aesEncryptionSpec.getAlgorithm());

            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher;
        } catch (
                NoSuchAlgorithmException |
                        InvalidKeySpecException |
                        InvalidKeyException |
                        InvalidAlgorithmParameterException |
                        NoSuchPaddingException ex) {
            throw new CryptoException(ex);
        }
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

}
