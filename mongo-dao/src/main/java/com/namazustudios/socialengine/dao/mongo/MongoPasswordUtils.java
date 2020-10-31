package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.ValidationGroups;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.experimental.updates.UpdateOperators;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
     * @param query the query to mutate
     * @param password the password
     */
    public void addPasswordToQuery(final Query<MongoUser> query, final String password) {

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

        query.update(UpdateOperators.set("salt", salt),
                UpdateOperators.set("passwordHash", digest.digest()),
                UpdateOperators.set("hashAlgorithm", digest.getAlgorithm())
        ).execute(new UpdateOptions().upsert(true));
    }

    /**
     * Scrambles both the salt and the password.  This effectively wipes out the account's
     * password making it inaccessible.
     *
     * @param query the query
     */
    public void scramblePassword(final Query<MongoUser> query) {

        final SecureRandom secureRandom = new SecureRandom();

        byte[] tmp;

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);

        final MessageDigest digest = getMessageDigestProvider().get();

        query.update(UpdateOperators.set("salt", tmp),
                UpdateOperators.set("passwordHash", tmp),
                UpdateOperators.set("hashAlgorithm", digest.getAlgorithm())
        ).execute(new UpdateOptions().upsert(true));

    }

    /**
     * Scrambles both the salt and the password.  This effectively wipes out the account's
     * password making it inaccessible.
     *
     * @param insertMap the map of objects to set on insert
     * @return a map with scrambled password added to it
     */
    public Map<String, Object> scramblePasswordOnInsert(final Map<String, Object> insertMap) {

        final SecureRandom secureRandom = new SecureRandom();

        byte[] tmp;

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        insertMap.put("salt", tmp);

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        insertMap.put("passwordHash", tmp);

        final MessageDigest digest = getMessageDigestProvider().get();
        insertMap.put("hashAlgorithm", digest.getAlgorithm());

        return insertMap;
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
