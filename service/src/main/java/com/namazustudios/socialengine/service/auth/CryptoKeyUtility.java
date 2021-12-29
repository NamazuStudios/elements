package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.auth.AuthSchemeAlgorithm;

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
     * Generates a key pair (public and private) as an {@link X509KeyPair}.
     *
     * @param authSchemeAlgorithm the {@link AuthSchemeAlgorithm} to use.
     *
     * @return the {@link X509KeyPair} including both public and private key.
     */
    X509KeyPair generateKeyPair(AuthSchemeAlgorithm authSchemeAlgorithm);

    /**
     *
     * @param authSchemeAlgorithm
     * @param base64Representation
     * @return
     */
    default PublicKey getPublicKey(final AuthSchemeAlgorithm authSchemeAlgorithm, final String base64Representation) {
        return getPublicKey(authSchemeAlgorithm, base64Representation, PublicKey.class);
    }

    /**
     * Loads the public key from the supplied
     *
     * @param authSchemeAlgorithm the auth scheme algorithm
     * @param base64Representation the base64 encoded public key
     * @param publicKeyTClass the public key class
     * @param <PublicKeyT> the requested type
     * @return the public key
     * @throws {@link com.namazustudios.socialengine.exception.auth.InvalidKeyException}
     */
    <PublicKeyT extends PublicKey> PublicKeyT getPublicKey(
            AuthSchemeAlgorithm authSchemeAlgorithm,
            String base64Representation,
            Class<PublicKeyT> publicKeyTClass);

}
