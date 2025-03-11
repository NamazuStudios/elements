package dev.getelements.elements.sdk.service.util;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncodedKeyPair {

    private final X509EncodedKeySpec publicKey;

    private final PKCS8EncodedKeySpec privateKey;

    public EncodedKeyPair(final KeyPair keyPair, final String algorithm) {
        this(keyPair.getPublic(), keyPair.getPrivate(), algorithm);
    }

    public EncodedKeyPair(final PublicKey publicKey, final PrivateKey privateKey, final String algorithm) {
        this(
            new X509EncodedKeySpec(publicKey.getEncoded()),
            new PKCS8EncodedKeySpec(privateKey.getEncoded(), algorithm)
        );
    }

    public EncodedKeyPair(final X509EncodedKeySpec publicKey, final PKCS8EncodedKeySpec privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public X509EncodedKeySpec getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
    }

    public PKCS8EncodedKeySpec getPrivateKey() {
        if (privateKey == null) throw new IllegalStateException("Private key not specified.");
        return privateKey;
    }

    public String getPrivateKeyBase64() {
        return Base64.getEncoder().encodeToString(getPrivateKey().getEncoded());
    }

}
