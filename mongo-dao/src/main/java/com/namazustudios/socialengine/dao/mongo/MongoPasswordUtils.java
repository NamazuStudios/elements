package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.InternalException;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Created by patricktwohig on 6/25/17.
 */
public class MongoPasswordUtils {

    public static final int SALT_LENGTH = 12;

    private static final SecureRandom generator = new SecureRandom();

    private String passwordEncoding;

    private Provider<MessageDigest> messageDigestProvider;

    /**
     * Generates salt and password hash according to the configuration.
     *
     * @param operations the operations to mutate
     * @param password the password
     */
    public void addPasswordToOperations(final UpdateOperations<MongoUser> operations, final String password) {

        final byte[] passwordBytes;

        try {
            passwordBytes = password.getBytes(getPasswordEncoding());
        } catch (UnsupportedEncodingException ex) {
            throw new InternalException(ex);
        }

        // Generate the hash

        final byte[] salt = new byte[SALT_LENGTH];
        generator.nextBytes(salt);

        final MessageDigest digest = getMessageDigestProvider().get();
        digest.update(salt);
        digest.update(passwordBytes);

        operations.set("salt", salt);
        operations.set("passwordHash", digest.digest());
        operations.set("hashAlgorithm", digest.getAlgorithm());

    }

    /**
     * Scrambles both the salt and the password.  This effectively wipes out the account's
     * password making it inaccessible.
     *
     * @param operations the operations
     */
    public void scramblePassword(final UpdateOperations<MongoUser> operations) {

        final SecureRandom secureRandom = new SecureRandom();

        byte[] tmp;

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        operations.set("salt", tmp);

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        operations.set("passwordHash", tmp);

        final MessageDigest digest = getMessageDigestProvider().get();
        operations.set("hashAlgorithm", digest.getAlgorithm());

    }

    /**
     * Scrambles both the salt and the password.  This effectively wipes out the account's
     * password making it inaccessible.
     *
     * @param operations the operations
     */
    public void scramblePasswordOnInsert(final UpdateOperations<MongoUser> operations) {

        final SecureRandom secureRandom = new SecureRandom();

        byte[] tmp;

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        operations.setOnInsert("salt", tmp);

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        operations.setOnInsert("passwordHash", tmp);

        final MessageDigest digest = getMessageDigestProvider().get();
        operations.setOnInsert("hashAlgorithm", digest.getAlgorithm());

    }

    /**
     * Given the instance of {@link MongoUser}, this will scramble the password making it
     * extremely impossible for a user to login.
     *
     * @param mongoUser the instance of {@link MongoUser}
     */
    public void scramblePassword(final MongoUser mongoUser) {

        final SecureRandom secureRandom = new SecureRandom();

        byte[] tmp;

        tmp = new byte[MongoPasswordUtils.SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        mongoUser.setSalt(tmp);

        tmp = new byte[MongoPasswordUtils.SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        mongoUser.setPasswordHash(tmp);

        final MessageDigest digest = newPasswordMessageDigest();
        mongoUser.setHashAlgorithm(digest.getAlgorithm());

    }

    /**
     * Creates a new {@link MessageDigest} instance used to hash passwords
     *
     * @return the {@link MessageDigest} used to make the password.
     */
    public MessageDigest newPasswordMessageDigest() {
        return getMessageDigestProvider().get();
    }

    public String getPasswordEncoding() {
        return passwordEncoding;
    }

    @Inject
    public void setPasswordEncoding(@Named(Constants.PASSWORD_ENCODING) String passwordEncoding) {
        this.passwordEncoding = passwordEncoding;
    }

    public Provider<MessageDigest> getMessageDigestProvider() {
        return messageDigestProvider;
    }

    @Inject
    public void setMessageDigestProvider(@Named(Constants.PASSWORD_DIGEST) Provider<MessageDigest> messageDigestProvider) {
        this.messageDigestProvider = messageDigestProvider;
    }

}
