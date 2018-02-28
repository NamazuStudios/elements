package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.Embedded;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * A type used to wrap up a securely generated key to use as an ID for a {@link MongoSession}.
 */
public class MongoSessionId {

    /**
     * The number of bytes that make up a sesion id.
     */
    public static final int SESSION_ID_LENGTH = 32;

    public static final int SESSION_ID_HASH_LENGTH = 16;

    private static final SecureRandom generator = new SecureRandom();

    private final byte[] id = new byte[SESSION_ID_LENGTH];

    public MongoSessionId() {}

    public MongoSessionId(final String sessionId) {

        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] id = decoder.decode(sessionId);

        if (id.length != SESSION_ID_HASH_LENGTH) {
            throw new IllegalArgumentException("Invalid session ID.");
        }

    }

    @Override
    public String toString() {
        final Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(id);
    }

}
