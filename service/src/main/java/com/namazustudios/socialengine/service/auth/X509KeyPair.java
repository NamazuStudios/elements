package com.namazustudios.socialengine.service.auth;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class X509KeyPair {

    private final X509EncodedKeySpec publicKey;

    private final X509EncodedKeySpec privateKey;

    public X509KeyPair(final KeyPair keyPair) {
        this(keyPair.getPublic(), keyPair.getPrivate());
    }

    public X509KeyPair(final PublicKey publicKey, final PrivateKey privateKey) {
        this(new X509EncodedKeySpec(publicKey.getEncoded()), new X509EncodedKeySpec(privateKey.getEncoded()));
    }

    public X509KeyPair(final X509EncodedKeySpec publicKey, final X509EncodedKeySpec privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public X509EncodedKeySpec getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
    }

    public X509EncodedKeySpec getPrivateKey() {
        if (privateKey == null) throw new IllegalStateException("Private key not specified.");
        return privateKey;
    }

    public String getPrivateKeyBase64() {
        return Base64.getEncoder().encodeToString(getPrivateKey().getEncoded());
    }

}
