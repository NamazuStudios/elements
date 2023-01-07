package com.namazustudios.socialengine.service.util;

import com.namazustudios.socialengine.exception.crypto.InvalidKeyException;
import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Utility functions for crypto keys.
 */
public interface CryptoKeyPairUtility {

    /**
     * The RSA Algorithm.
     */
    String RSA_ALGO = "RSA";

    /**
     * The Elliptic Curve Algorithm.
     */
    String ECDSA_ALGO = "EC";

    /**
     * Generates a key pair (public and private) as an {@link EncodedKeyPair}.
     *
     * @param privateKeyCrytpoAlgorithm the {@link PrivateKeyCrytpoAlgorithm} to use.
     *
     * @return the {@link EncodedKeyPair} including both public and private key.
     */
    EncodedKeyPair generateKeyPair(PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm);

    /**
     * Loads the public key from the supplied base64 string.
     *
     * @param privateKeyCrytpoAlgorithm the algorithm
     * @param base64Representation the base64 representation
     * @return the {@link PublicKey}
     */
    default PublicKey getPublicKey(final PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
                                   final String base64Representation)  throws InvalidKeyException {
        return getPublicKey(privateKeyCrytpoAlgorithm, base64Representation, PublicKey.class);
    }

    /**
     * Loads the public key from the supplied base64 encoded string.
     *
     * @param privateKeyCrytpoAlgorithm the auth scheme algorithm
     * @param base64Representation the base64 encoded public key
     * @param publicKeyTClass the public key class
     * @param <PublicKeyT> the requested type
     * @return the public key
     */
    <PublicKeyT extends PublicKey> PublicKeyT getPublicKey(
            PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
            String base64Representation,
            Class<PublicKeyT> publicKeyTClass) throws InvalidKeyException;

    /**
     * Loads the private key from the supplied base64 string.
     *
     * @param privateKeyCrytpoAlgorithm the algorithm
     * @param base64Representation the base64 representation
     * @return the {@link PublicKey}
     */
    default PrivateKey getPrivateKey(final PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
                                     final String base64Representation)  throws InvalidKeyException {
        return getPrivateKey(privateKeyCrytpoAlgorithm, base64Representation, PrivateKey.class);
    }

    /**
     * Loads the private key from the supplied base64 encoded string.
     *
     * @param privateKeyCrytpoAlgorithm the auth scheme algorithm
     * @param base64Representation the base64 encoded public key
     * @param publicKeyTClass the public key class
     * @param <PrivateKeyT> the requested type
     * @return the {@link PrivateKey}
     */
    <PrivateKeyT extends PrivateKey> PrivateKeyT getPrivateKey(
            PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
            String base64Representation,
            Class<PrivateKeyT> publicKeyTClass) throws InvalidKeyException;

}
