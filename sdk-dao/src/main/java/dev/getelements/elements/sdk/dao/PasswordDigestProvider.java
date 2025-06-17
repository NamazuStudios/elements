package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Constants;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by patricktwohig on 4/6/15.
 */
public class PasswordDigestProvider implements Provider<MessageDigest> {

    @Inject
    @Named(Constants.PASSWORD_DIGEST_ALGORITHM)
    private String digestAlgorithm;

    @Override
    public MessageDigest get() {
        try {
            return MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
