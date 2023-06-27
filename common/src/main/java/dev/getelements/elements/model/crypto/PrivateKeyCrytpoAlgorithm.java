package dev.getelements.elements.model.crypto;

import dev.getelements.elements.model.auth.AuthScheme;

/**
 * Represents the signing algorithm for the {@link AuthScheme}.
 */
public enum PrivateKeyCrytpoAlgorithm {

    /**
     * RSA 256-bit
     */
    RSA_256,

    /**
     * RSA 384-bit
     */
    RSA_384,

    /**
     * RSA 512-bit
     */
    RSA_512,

    /**
     * Elliptic Curve 256-bit
     */
    ECDSA_256,

    /**
     * Elliptic Curve 384-bit
     */
    ECDSA_384,

    /**
     * Elliptic Curve 512-bit
     */
    ECDSA_512

}
