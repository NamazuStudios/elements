package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.rt.util.Hex;
import org.bson.types.ObjectId;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.CRC32;

import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;

/**
 * A type used to wrap up a securely generated key to use as an ID for a {@link MongoSession}.
 */
public class MongoSessionSecret {

    private static final SecureRandom generator = new SecureRandom();

    /**
     * The number of bytes that make up a session secret.
     */
    public static final int SESSION_ID_LENGTH = 16;

    /**
     * The number of bytes that make up the session secret checksum header.
     */
    public static final int SESSION_CHECKSUM_LENGTH = 4;

    /**
     * Represents the secret total length.
     */
    public static final int SECRET_HEADER_LENGTH = SESSION_CHECKSUM_LENGTH + SESSION_ID_LENGTH;

    private final byte[] secret;

    /**
     * Initializes a context-based {@link MongoSessionSecret} using an {@link ObjectId} as a context.  The supplied
     * {@link ObjectId} may be retrieved later to be used as a means to identify what created the
     * {@link MongoSessionSecret}.
     *
     * @param objectId the {@link ObjectId}.
     */
    public MongoSessionSecret(final ObjectId objectId) {
        this(objectId.toByteArray());
    }

    /**
     * Initializes a context-based {@link MongoSessionSecret}.  The context is an extra set of bytes used to append some
     * sort if identity to a particular {@link MongoSessionSecret}.  This can be, for example, an {@link ObjectId} for
     * a {@link MongoUser}.
     *
     * @param context
     */
    public MongoSessionSecret(final byte[] context) {
        secret = new byte[SECRET_HEADER_LENGTH + context.length];
        generator.nextBytes(secret);
        fill(secret, 0, SESSION_CHECKSUM_LENGTH, (byte)0);
        arraycopy(context, 0, secret, SECRET_HEADER_LENGTH, context.length);
        writeChecksumHeader(secret);
    }

    public MongoSessionSecret(final String sessionSecret) {

        final byte[] parsedSecret = Hex.decode(sessionSecret);

        if (parsedSecret.length < SECRET_HEADER_LENGTH) {
            throw new IllegalArgumentException("Invalid Session Secret.");
        }

        secret = new byte[parsedSecret.length];
        arraycopy(parsedSecret, 0, secret, 0, parsedSecret.length);
        fill(parsedSecret, 0, SESSION_CHECKSUM_LENGTH, (byte)0);

        writeChecksumHeader(parsedSecret);

        if (!Arrays.equals(parsedSecret, secret)) {
            throw new IllegalArgumentException("Invalid Session Secret.");
        }

    }

    private void writeChecksumHeader(final byte[] bytes) {
        final CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        writeChecksumHeader(bytes, crc32.getValue());
    }

    private void writeChecksumHeader(final byte[] bytes, final long checksum) {
        bytes[0] = (byte) (0xFF & (checksum >> 8 * 0));
        bytes[1] = (byte) (0xFF & (checksum >> 8 * 1));
        bytes[2] = (byte) (0xFF & (checksum >> 8 * 2));
        bytes[3] = (byte) (0xFF & (checksum >> 8 * 3));
    }

     /**
     * Gets the raw secret value as determined suitable for use in {@link SessionCreation#setSessionSecret(String)}.
     * Take care that the returned value is a secret and should not be logged or transmitted over an untrusted network
     * in plain text.
     *
     * @return the secret token
     */
    public String getSessionSecret() {
        return Hex.encode(secret);
    }

    /**
     * Returns the context used to create this {@link MongoSessionSecret}.  See the notes on
     * {@link #MongoSessionSecret(byte[])} for what a context is.
     *
     * @return the context bytes
     */
    public byte[] getContext() {
        final byte[] context = new byte[secret.length - SECRET_HEADER_LENGTH];
        arraycopy(secret, SECRET_HEADER_LENGTH, context, 0, context.length);
        return context;
    }

    /**
     * Reads the context of this {@link MongoSessionSecret} and parses it as an {@link ObjectId} using
     * {@link ObjectId#ObjectId(byte[])}.
     *
     * @return the context as {@link ObjectId}
     */
    public ObjectId getContextAsObjectId() {
        final byte[] context = getContext();
        return new ObjectId(context);
    }

    /**
     * Gets a digest version of this {@link MongoSessionSecret} using the provided {@link MessageDigest}.  This allows
     * the {@link MongoSessionSecret} to be stored as a hash key in the database.
     *
     * @param messageDigest the {@link MessageDigest} used to calcualte the hash
     * @param salt a value to prepend to the digest before generation
     * @return the secret hash
     */
    public byte[] getSecretDigest(final MessageDigest messageDigest, final byte[] salt) {
        messageDigest.update(salt);
        messageDigest.update(secret);
        return messageDigest.digest();
    }

    /**
     * Base64 encodes the secret digest using {@link #getSecretDigestEncoded(MessageDigest, byte[])} and returns the
     * result.
     *
     * @param messageDigest the {@link MessageDigest} instance.
     * @param salt a value to prepend to the digest before generation
     * @return the secret hash
     */
    public String getSecretDigestEncoded(final MessageDigest messageDigest, final byte[] salt) {
        final byte[] bytes = getSecretDigest(messageDigest, salt);
        return Hex.encode(bytes);
    }

}
