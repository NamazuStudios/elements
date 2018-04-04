package com.namazustudios.socialengine.dao.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

import java.util.Base64;
import java.util.function.Function;

import static java.lang.System.arraycopy;

/**
 * Represents a unique id between two {@link MongoUser} instances, forming a {@link MongoFriend}.  This derives the
 * ID value itself as a compound value
 */
public class MongoFriendId {

    public static final int VERSION = 0;

    public static final int VERSION_LENGTH = 1;

    public static final int OID_LENGTH_BYTES = 12;

    public static final int LENGTH_BYTES = (2 * OID_LENGTH_BYTES) + VERSION_LENGTH;

    /**
     * Attempts to parse the supplied string into a {@link MongoFriendId}.  If parsing fails, then the supplied
     * {@link Function<Throwable, ExceptionT>} will be used to generate an {@link Exception} to throw.
     *
     * @param hexStringRepresentation the hex string representation of the {@link MongoFriendId}
     * @param exceptionSupplier the {@link Function<Throwable, ExceptionT>} to generate an exception if necessary
     * @param <ExceptionT> the exception type
     * @return the {@link MongoFriendId} instance (never null).
     * @throws ExceptionT if parsing fails
     */
    public static <ExceptionT extends Exception> MongoFriendId parseOrThrow(
            final String hexStringRepresentation,
            final Function<Throwable, ExceptionT> exceptionSupplier) throws ExceptionT {
        try {
            return new MongoFriendId(hexStringRepresentation);
        } catch (IllegalArgumentException ex) {
            throw exceptionSupplier.apply(ex);
        }
    }

    @Indexed
    @Property
    private ObjectId user;

    @Indexed
    @Property
    private ObjectId friend;

    public MongoFriendId() {}

    public MongoFriendId(final ObjectId userObjectId, final ObjectId friendObjectId) {
        this.user = userObjectId;
        this.friend = friendObjectId;
    }

    public MongoFriendId(final String hexStringRepresentation) {

        final byte[] bytes = Base64.getDecoder().decode(hexStringRepresentation);

        if (bytes.length != LENGTH_BYTES) {
            throw new IllegalArgumentException("Invalid Friend ID ength.");
        }

        byte version = bytes[0];

        if (version != 0) {
            throw new IllegalArgumentException("Invalid Friend ID Version: " + version);
        }

        final byte[] userBytes = new byte[OID_LENGTH_BYTES];
        arraycopy(bytes, VERSION_LENGTH, userBytes, 0, OID_LENGTH_BYTES);

        final byte[] friendBytes = new byte[OID_LENGTH_BYTES];
        arraycopy(bytes, VERSION_LENGTH + OID_LENGTH_BYTES, friendBytes,0, OID_LENGTH_BYTES);

        this.user = new ObjectId(userBytes);
        this.friend = new ObjectId(friendBytes);

    }

    public ObjectId getUser() {
        return user;
    }

    public void setUser(ObjectId user) {
        this.user = user;
    }

    public ObjectId getFriend() {
        return friend;
    }

    public void setFriend(ObjectId friend) {
        this.friend = friend;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoFriendId)) return false;

        MongoFriendId that = (MongoFriendId) o;

        if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) return false;
        return getFriend() != null ? getFriend().equals(that.getFriend()) : that.getFriend() == null;
    }

    @Override
    public int hashCode() {
        int result = getUser() != null ? getUser().hashCode() : 0;
        result = 31 * result + (getFriend() != null ? getFriend().hashCode() : 0);
        return result;
    }

    public byte[] toByteArray() {

        final byte[] userObjectIdBytes;

        try {
            userObjectIdBytes = getUser().toByteArray();
        } catch (NullPointerException ex) {
            throw new IllegalStateException("User null", ex);
        }

        final byte[] friendObjectIdBytes;

        try {
            friendObjectIdBytes = getFriend().toByteArray();
        } catch (NullPointerException ex) {
            throw new IllegalStateException("Friend null", ex);
        }

        final byte[] out = new byte[LENGTH_BYTES];

        out[0] = VERSION;
        arraycopy(userObjectIdBytes, 0, out, VERSION_LENGTH, OID_LENGTH_BYTES);
        arraycopy(friendObjectIdBytes, 0, out, VERSION_LENGTH + OID_LENGTH_BYTES, OID_LENGTH_BYTES);

        return out;

    }

    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

}
