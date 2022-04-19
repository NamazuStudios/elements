package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.dao.mongo.HexableId;
import com.namazustudios.socialengine.rt.util.Hex;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.util.Base64;
import java.util.function.Function;

import static com.namazustudios.socialengine.dao.mongo.MongoConstants.OID_LENGTH_BYTES;
import static java.lang.System.arraycopy;

/**
 * Represents a unique id between two {@link MongoUser} instances, forming a {@link MongoFriendship}.  This derives the
 * ID value itself as a compound value
 */
@Embedded
public class MongoFriendshipId implements HexableId {

    public static final int VERSION = 0;

    public static final int VERSION_LENGTH = 1;

    public static final int LENGTH_BYTES = (2 * OID_LENGTH_BYTES) + VERSION_LENGTH;

    /**
     * Attempts to parse the supplied string into a {@link MongoFriendshipId}.  If parsing fails, then the supplied
     * {@link Function<Throwable, ExceptionT>} will be used to generate an {@link Exception} to throw.
     *
     * @param hexStringRepresentation the hex string representation of the {@link MongoFriendshipId}
     * @param exceptionSupplier the {@link Function<Throwable, ExceptionT>} to generate an exception if necessary
     * @param <ExceptionT> the exception type
     * @return the {@link MongoFriendshipId} instance (never null).
     * @throws ExceptionT if parsing fails
     */
    public static <ExceptionT extends Exception> MongoFriendshipId parseOrThrow(
            final String hexStringRepresentation,
            final Function<Throwable, ExceptionT> exceptionSupplier) throws ExceptionT {
        try {
            return new MongoFriendshipId(hexStringRepresentation);
        } catch (IllegalArgumentException ex) {
            throw exceptionSupplier.apply(ex);
        }
    }

    public static ObjectId lesser(final ObjectId a, final ObjectId b) {
        if (a.compareTo(b) == 0) throw new IllegalArgumentException("Identifiers must not match.");
        return a.compareTo(b) < 0 ? a : b;
    }

    public static ObjectId greater(final ObjectId a, final ObjectId b) {
        if (a.compareTo(b) == 0) throw new IllegalArgumentException("Identifiers must not match.");
        return a.compareTo(b) > 0 ? a : b;
    }

    @Indexed
    @Property
    private ObjectId lesser;

    @Indexed
    @Property
    private ObjectId greater;

    public MongoFriendshipId() {}

    public MongoFriendshipId(final ObjectId userA, final ObjectId userB) {
        this.lesser = lesser(userA, userB);
        this.greater = greater(userA, userB);
    }

    public MongoFriendshipId(final String hexStringRepresentation) {

        final byte[] bytes = Hex.decode(hexStringRepresentation);

        if (bytes.length != LENGTH_BYTES) {
            throw new IllegalArgumentException("Invalid Friend ID ength.");
        }

        byte version = bytes[0];

        if (version != 0) {
            throw new IllegalArgumentException("Invalid Friend ID Version: " + version);
        }

        final byte[] lesserBytes = new byte[OID_LENGTH_BYTES];
        arraycopy(bytes, VERSION_LENGTH, lesserBytes, 0, OID_LENGTH_BYTES);

        final byte[] greaterBytes = new byte[OID_LENGTH_BYTES];
        arraycopy(bytes, VERSION_LENGTH + OID_LENGTH_BYTES, greaterBytes,0, OID_LENGTH_BYTES);

        this.lesser = new ObjectId(lesserBytes);
        this.greater = new ObjectId(greaterBytes);

    }

    public ObjectId getLesser() {
        return lesser;
    }

    public ObjectId getGreater() {
        return greater;
    }

    public ObjectId getOpposite(final ObjectId objectId) {
        return getLesser().equals(objectId) ? getGreater() : getLesser();
    }

    public byte[] toByteArray() {

        final byte[] lesserBytes;

        try {
            lesserBytes = getLesser().toByteArray();
        } catch (NullPointerException ex) {
            throw new IllegalStateException("User null", ex);
        }

        final byte[] greaterBytes;

        try {
            greaterBytes = getGreater().toByteArray();
        } catch (NullPointerException ex) {
            throw new IllegalStateException("Friend null", ex);
        }

        final byte[] out = new byte[LENGTH_BYTES];

        out[0] = VERSION;
        arraycopy(lesserBytes, 0, out, VERSION_LENGTH, OID_LENGTH_BYTES);
        arraycopy(greaterBytes, 0, out, VERSION_LENGTH + OID_LENGTH_BYTES, OID_LENGTH_BYTES);

        return out;

    }

    @Override
    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Hex.encode(bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoFriendshipId)) return false;

        MongoFriendshipId that = (MongoFriendshipId) o;

        if (getLesser() != null ? !getLesser().equals(that.getLesser()) : that.getLesser() != null) return false;
        return getGreater() != null ? getGreater().equals(that.getGreater()) : that.getGreater() == null;
    }

    @Override
    public int hashCode() {
        int result = getLesser() != null ? getLesser().hashCode() : 0;
        result = 31 * result + (getGreater() != null ? getGreater().hashCode() : 0);
        return result;
    }

}
