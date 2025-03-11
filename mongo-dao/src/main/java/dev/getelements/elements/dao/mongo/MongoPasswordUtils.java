package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.model.exception.InternalException;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.setOnInsert;

/**
 * Created by patricktwohig on 6/25/17.
 */
public class MongoPasswordUtils {

    public static final int SALT_LENGTH = 12;

    private static final SecureRandom generator = new SecureRandom();

    private String passwordEncoding;

    private Provider<MessageDigest> messageDigestProvider;

    /**
     * Generates salt and password hash according to the configuration and adds the required update operations to the
     * supplied {@link UpdateBuilder} instance.
     *
     * @param builder the {@link UpdateBuilder} to mutate
     * @param password the password
     */
    public UpdateBuilder addPasswordToBuilder(final UpdateBuilder builder, final String password) {

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

        return builder.with(
            set("salt", salt),
            set("passwordHash", digest.digest()),
            set("hashAlgorithm", digest.getAlgorithm())
        );

    }

    /**
     * Scrambles both the salt and the password.  This effectively wipes out the account's
     * password making it inaccessible.
     *
     * @param builder the {@link UpdateBuilder}
     */
    public UpdateBuilder scramblePassword(final UpdateBuilder builder) {

        byte[] tmp;

        tmp = new byte[SALT_LENGTH];
        generator.nextBytes(tmp);

        tmp = new byte[SALT_LENGTH];
        generator.nextBytes(tmp);

        final MessageDigest digest = getMessageDigestProvider().get();

        return builder.with(
            set("salt", tmp),
            set("passwordHash", tmp),
            set("hashAlgorithm", digest.getAlgorithm())
        );

    }

    /**
     * Scrambles both the salt and the password.
     *
     * @return a map with scrambled password added to it
     */
    public Map<String, Object> scramblePasswordOnInsert() {
        return scramblePasswordOnInsert(new HashMap<>());
    }

    /**
     * Scrambles both the salt and the password.
     *
     * @param insertMap the map of objects to set on insert
     * @return a map with scrambled password added to it
     */
    public Map<String, Object> scramblePasswordOnInsert(final Map<String, Object> insertMap) {

        byte[] tmp;

        tmp = new byte[SALT_LENGTH];
        generator.nextBytes(tmp);
        insertMap.put("salt", tmp);

        tmp = new byte[SALT_LENGTH];
        generator.nextBytes(tmp);
        insertMap.put("passwordHash", tmp);

        final MessageDigest digest = getMessageDigestProvider().get();
        insertMap.put("hashAlgorithm", digest.getAlgorithm());

        return insertMap;
    }


    /**
     * Scrambles both the salt and the password.
     *
     * @param builder the {@link UpdateBuilder}
     */
    public UpdateBuilder scramblePasswordOnInsert(final UpdateBuilder builder) {
        final var fields = scramblePasswordOnInsert();
        return builder.with(setOnInsert(fields));
    }

    /**
     * Given the instance of {@link MongoUser}, this will scramble the password making it
     * extremely impossible for a user to login.
     *
     * @param mongoUser the instance of {@link MongoUser}
     */
    public void scramblePassword(final MongoUser mongoUser) {

        byte[] tmp;

        tmp = new byte[MongoPasswordUtils.SALT_LENGTH];
        generator.nextBytes(tmp);
        mongoUser.setSalt(tmp);

        tmp = new byte[MongoPasswordUtils.SALT_LENGTH];
        generator.nextBytes(tmp);
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

    public Charset getPasswordEncodingCharset() {
        try {
            return Charset.forName(getPasswordEncoding());
        } catch (UnsupportedCharsetException ex) {
            throw new InternalException(ex);
        }
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
