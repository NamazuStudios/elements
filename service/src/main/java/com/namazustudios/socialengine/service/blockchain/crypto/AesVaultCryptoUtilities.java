package com.namazustudios.socialengine.service.blockchain.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.crypto.CryptoException;
import com.namazustudios.socialengine.model.blockchain.wallet.VaultKey;
import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;
import com.namazustudios.socialengine.rt.util.Hex;
import com.namazustudios.socialengine.service.util.CryptoKeyPairUtility;

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

import static com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;

public class AesVaultCryptoUtilities implements VaultCryptoUtilities {

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

    private CryptoKeyPairUtility cryptoKeyPairUtility;

    @Override
    public VaultKey generateKey(final PrivateKeyCrytpoAlgorithm algorithm) {

        final var encodedKeyPair = getCryptoKeyPairUtility().generateKeyPair(algorithm);

        final var vaultKey = new VaultKey();
        vaultKey.setEncrypted(false);
        vaultKey.setAlgorithm(algorithm);
        vaultKey.setPublicKey(encodedKeyPair.getPublicKeyBase64());
        vaultKey.setPrivateKey(encodedKeyPair.getPrivateKeyBase64());

        return vaultKey;

    }

    @Override
    public VaultKey encryptKey(final VaultKey vaultKey, final String passphrase) {

        if (vaultKey.isEncrypted()) {
            throw new IllegalArgumentException("Vault key is already encrypted.");
        }

        if (!getValidator().validate(vaultKey).isEmpty()) {
            throw new IllegalArgumentException("Invalid vault key.");
        }

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

        final var encrypted = getObjectMapper().convertValue(vaultKey, VaultKey.class);

        final var encryption = getObjectMapper().convertValue(aesEncryptionSpec, Map.class);
        encrypted.setEncrypted(true);
        encrypted.setEncryption(encryption);

        try {
            final var cipher = getCipher(iv, salt, passphrase, aesEncryptionSpec, Cipher.ENCRYPT_MODE);
            final var unencryptedBytes = vaultKey.getPrivateKey().getBytes(StandardCharsets.UTF_8);
            final var encryptedBytes = cipher.doFinal(unencryptedBytes);
            final var encryptedString = Hex.encode(encryptedBytes);
            encrypted.setPrivateKey(encryptedString);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            throw new CryptoException(ex);
        }

        return encrypted;

    }

    @Override
    public Optional<VaultKey> decryptKey(final VaultKey vaultKey, final String passphrase) {

        final var encryption = vaultKey.getEncryption();

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

        final var decrypted = getObjectMapper().convertValue(vaultKey, VaultKey.class);
        decrypted.setEncrypted(false);
        decrypted.setEncryption(null);

        try {
            final var encrypted = vaultKey.getPrivateKey();
            final var cipher = getCipher(iv, salt, passphrase, aesEncryptionSpec, Cipher.DECRYPT_MODE);
            final var encryptedBytes = Hex.decode(encrypted);
            final var decryptedBytes = cipher.doFinal(encryptedBytes);
            final var decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
            decrypted.setPrivateKey(decryptedString);
            return Optional.of(decrypted);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            return Optional.empty();
        }

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

    public CryptoKeyPairUtility getCryptoKeyPairUtility() {
        return cryptoKeyPairUtility;
    }

    @Inject
    public void setCryptoKeyPairUtility(CryptoKeyPairUtility cryptoKeyPairUtility) {
        this.cryptoKeyPairUtility = cryptoKeyPairUtility;
    }

}
