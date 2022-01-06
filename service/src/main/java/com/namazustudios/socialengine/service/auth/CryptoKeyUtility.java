package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.exception.auth.InvalidKeyException;
import com.namazustudios.socialengine.model.auth.AuthSchemeAlgorithm;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Utility functions for crypto keys.
 */
public interface CryptoKeyUtility {

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
     * @param authSchemeAlgorithm the {@link AuthSchemeAlgorithm} to use.
     *
     * @return the {@link EncodedKeyPair} including both public and private key.
     */
    EncodedKeyPair generateKeyPair(AuthSchemeAlgorithm authSchemeAlgorithm);

    /**
     * Loads the public key from the supplied base64 string.
     *
     * @param authSchemeAlgorithm the algorithm
     * @param base64Representation the base64 representation
     * @return the {@link PublicKey}
     */
    default PublicKey getPublicKey(final AuthSchemeAlgorithm authSchemeAlgorithm,
                                   final String base64Representation)  throws InvalidKeyException {
        return getPublicKey(authSchemeAlgorithm, base64Representation, PublicKey.class);
    }

    /**
     * Loads the public key from the supplied base64 encoded string.
     *
     * @param authSchemeAlgorithm the auth scheme algorithm
     * @param base64Representation the base64 encoded public key
     * @param publicKeyTClass the public key class
     * @param <PublicKeyT> the requested type
     * @return the public key
     */
    <PublicKeyT extends PublicKey> PublicKeyT getPublicKey(
            AuthSchemeAlgorithm authSchemeAlgorithm,
            String base64Representation,
            Class<PublicKeyT> publicKeyTClass) throws InvalidKeyException;

    /**
     * Loads the private key from the supplied base64 string.
     *
     * @param authSchemeAlgorithm the algorithm
     * @param base64Representation the base64 representation
     * @return the {@link PublicKey}
     */
    default PrivateKey getPrivateKey(final AuthSchemeAlgorithm authSchemeAlgorithm,
                                     final String base64Representation)  throws InvalidKeyException {
        return getPrivateKey(authSchemeAlgorithm, base64Representation, PrivateKey.class);
    }

    /**
     * Loads the private key from the supplied base64 encoded string.
     *
     * @param authSchemeAlgorithm the auth scheme algorithm
     * @param base64Representation the base64 encoded public key
     * @param publicKeyTClass the public key class
     * @param <PrivateKeyT> the requested type
     * @return the {@link PrivateKey}
     */
    <PrivateKeyT extends PrivateKey> PrivateKeyT getPrivateKey(
            AuthSchemeAlgorithm authSchemeAlgorithm,
            String base64Representation,
            Class<PrivateKeyT> publicKeyTClass) throws InvalidKeyException;

}
