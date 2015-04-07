package com.namazustudios.promotion.dao;

import com.namazustudios.promotion.Constants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
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
            return  MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
