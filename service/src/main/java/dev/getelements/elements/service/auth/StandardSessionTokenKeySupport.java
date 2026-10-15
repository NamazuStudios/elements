package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.dao.JwtSigningKeyDao;
import dev.getelements.elements.sdk.model.auth.JwtSigningKey;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.service.Constants;
import dev.getelements.elements.rt.util.Hex;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.currentTimeMillis;

/**
 * Loads, caches, and generates the server's own session-token signing keys. Keys are cached in
 * memory so that the (deliberately expensive) key-derivation performed when decrypting private key
 * material does not occur on every request; the cache refreshes periodically so that key rotation
 * is picked up without a restart. An unknown {@code kid} forces an immediate cache refresh, mirroring
 * the cache-on-miss behavior of the OIDC JWKS path.
 */
public class StandardSessionTokenKeySupport {

    private static final int IV_LENGTH = 16;

    private static final int SALT_LENGTH = 32;

    private static final int AES_KEY_LENGTH = 256;

    private static final int AES_ITERATIONS = 65536;

    private static final String AES = "AES";

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final SecureRandom secureRandom = new SecureRandom();

    private final Object keyCacheLock = new Object();

    private final Map<String, JwtSigningKey> keyCache = new ConcurrentHashMap<>();

    private volatile long keyCacheRefreshAt = 0;

    private JwtSigningKeyDao jwtSigningKeyDao;

    private String passphrase;

    private long keyRefreshSeconds;

    /**
     * Returns the active signing key, generating and persisting a new one if no active key exists.
     *
     * @return the active {@link JwtSigningKey}, with its private half decrypted
     */
    public JwtSigningKey getActiveSigningKey() {

        final var activeKeys = getJwtSigningKeyDao().getSigningKeys(JwtSigningKey.Status.ACTIVE);

        if (!activeKeys.isEmpty()) {
            return activeKeys.get(0);
        }

        return bootstrapNewSigningKey();

    }

    /**
     * Returns the public key material for the supplied {@code kid}, refreshing the key cache when
     * the kid is unknown.
     *
     * @param kid the key id from the token's JOSE header
     * @return the {@link RSAPublicKey}, never null
     */
    public RSAPublicKey getPublicKey(final String kid) {

        var signingKey = getCachedKey(kid);

        if (signingKey == null) {
            refreshKeyCache();
            signingKey = getCachedKey(kid);
        }

        if (signingKey == null) {
            throw new UnauthorizedException("Unknown signing key id.");
        }

        return decodePublicKey(signingKey.getPublicKey());

    }

    /**
     * Decrypts and returns the private key material of the supplied {@link JwtSigningKey}.
     *
     * @param jwtSigningKey the signing key
     * @return the {@link RSAPrivateKey}, never null
     */
    public RSAPrivateKey getPrivateKey(final JwtSigningKey jwtSigningKey) {
        return decodePrivateKey(jwtSigningKey.getPrivateKey(), jwtSigningKey.getEncryption());
    }

    private JwtSigningKey getCachedKey(final String kid) {

        final var now = currentTimeMillis();

        if (now >= keyCacheRefreshAt) {
            refreshKeyCache();
        }

        return keyCache.get(kid);

    }

    private void refreshKeyCache() {
        synchronized (keyCacheLock) {

            final var now = currentTimeMillis();

            if (now < keyCacheRefreshAt) return;

            final var keys = getJwtSigningKeyDao().getSigningKeys();
            keyCache.clear();
            keys.forEach(key -> keyCache.put(key.getKid(), key));
            keyCacheRefreshAt = now + (getKeyRefreshSeconds() * 1000);

        }
    }

    private synchronized JwtSigningKey bootstrapNewSigningKey() {

        // Re-check under the lock: another node (or thread) may have created the first key while
        // this one was generating its own candidate.

        final var existing = getJwtSigningKeyDao().getSigningKeys(JwtSigningKey.Status.ACTIVE);
        if (!existing.isEmpty()) return existing.get(0);

        try {

            final var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(JwtSigningKey.RSA_KEY_LENGTH, secureRandom);
            final KeyPair keyPair = keyPairGenerator.generateKeyPair();

            final var publicKey = (RSAPublicKey) keyPair.getPublic();
            final var privateKey = (RSAPrivateKey) keyPair.getPrivate();

            final var keyFactory = KeyFactory.getInstance("RSA");
            final var publicKeyBase64 = Base64.getEncoder()
                    .encodeToString(keyFactory.getKeySpec(publicKey, X509EncodedKeySpec.class).getEncoded());
            final var privateKeyBase64 = Base64.getEncoder()
                    .encodeToString(keyFactory.getKeySpec(privateKey, PKCS8EncodedKeySpec.class).getEncoded());

            final var kid = generateKid();

            final var jwtSigningKey = new JwtSigningKey();
            jwtSigningKey.setKid(kid);
            jwtSigningKey.setAlgorithm(JwtSigningKey.ALGORITHM);
            jwtSigningKey.setStatus(JwtSigningKey.Status.ACTIVE);
            jwtSigningKey.setPublicKey(publicKeyBase64);
            jwtSigningKey.setCreatedAt(currentTimeMillis());

            encryptPrivateKey(jwtSigningKey, privateKeyBase64);

            final var saved = getJwtSigningKeyDao().createSigningKey(jwtSigningKey);

            synchronized (keyCacheLock) {
                keyCache.put(saved.getKid(), saved);
            }

            return saved;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new InternalException(ex);
        }

    }

    private static String generateKid() {
        final var bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return Hex.encode(bytes);
    }

    void encryptPrivateKey(final JwtSigningKey jwtSigningKey, final String privateKey) {

        final var iv = new byte[IV_LENGTH];
        final var salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(iv);
        secureRandom.nextBytes(salt);

        try {

            final var cipher = getCipher(iv, salt, Cipher.ENCRYPT_MODE);
            final var encryptedBytes = cipher.doFinal(privateKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            final var encryption = new JwtSigningKey.Encryption();
            encryption.setFamily(AES);
            encryption.setAlgorithm(AES_ALGORITHM);
            encryption.setSecretKeyAlgorithm(SECRET_KEY_ALGORITHM);
            encryption.setIv(Hex.encode(iv));
            encryption.setSalt(Hex.encode(salt));
            encryption.setIterations(AES_ITERATIONS);
            encryption.setKeyLength(AES_KEY_LENGTH);

            jwtSigningKey.setPrivateKey(Hex.encode(encryptedBytes));
            jwtSigningKey.setEncryption(encryption);

        } catch (final Exception ex) {
            throw new InternalException(ex);
        }

    }

    private RSAPrivateKey decodePrivateKey(final String encryptedPrivateKey, final JwtSigningKey.Encryption encryption) {

        if (encryption == null) {
            throw new InternalException("Signing key is not encrypted.");
        }

        try {

            final var iv = Hex.decode(encryption.getIv());
            final var salt = Hex.decode(encryption.getSalt());

            final var cipher = getCipher(iv, salt, Cipher.DECRYPT_MODE);
            final var decryptedBytes = cipher.doFinal(Hex.decode(encryptedPrivateKey));
            final var privateKeyBase64 = new String(decryptedBytes, java.nio.charset.StandardCharsets.UTF_8);

            final var keyFactory = KeyFactory.getInstance("RSA");
            final var keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64));

            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

        } catch (final Exception ex) {
            throw new InternalException(ex);
        }

    }

    private RSAPublicKey decodePublicKey(final String publicKeyBase64) {
        try {
            final var keyFactory = KeyFactory.getInstance("RSA");
            final var keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64));
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new InternalException(ex);
        }
    }

    private Cipher getCipher(final byte[] iv, final byte[] salt, final int mode) {
        try {

            final var chars = passphrase.toCharArray();
            final var keySpec = new PBEKeySpec(chars, salt, AES_ITERATIONS, AES_KEY_LENGTH);
            final var factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
            final var secret = factory.generateSecret(keySpec);
            final var secretKeySpec = new SecretKeySpec(secret.getEncoded(), AES);
            final var ivParameterSpec = new IvParameterSpec(iv);
            final var cipher = Cipher.getInstance(AES_ALGORITHM);

            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher;

        } catch (final Exception ex) {
            throw new InternalException(ex);
        }
    }

    public JwtSigningKeyDao getJwtSigningKeyDao() {
        return jwtSigningKeyDao;
    }

    @Inject
    public void setJwtSigningKeyDao(JwtSigningKeyDao jwtSigningKeyDao) {
        this.jwtSigningKeyDao = jwtSigningKeyDao;
    }

    public String getPassphrase() {
        return passphrase;
    }

    @Inject
    public void setPassphrase(@Named(Constants.SESSION_TOKEN_KEY_PASSPHRASE) String passphrase) {
        this.passphrase = passphrase;
    }

    public long getKeyRefreshSeconds() {
        return keyRefreshSeconds;
    }

    @Inject
    public void setKeyRefreshSeconds(@Named(Constants.SESSION_TOKEN_KEY_REFRESH_SECONDS) long keyRefreshSeconds) {
        this.keyRefreshSeconds = keyRefreshSeconds;
    }

}
